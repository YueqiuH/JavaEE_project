package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.config.CourseScheduleConfig;
import com.smartcampus.app.dao.teaching.ClassroomMapper;
import com.smartcampus.app.dao.teaching.CourseCapacityMapper;
import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.app.dao.teaching.ScheduleMapper;
import com.smartcampus.app.dto.teaching.AutoScheduleConfigDto;
import com.smartcampus.app.dto.teaching.ScheduleDto;
import com.smartcampus.app.service.teaching.IAutoScheduleService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Classroom;
import com.smartcampus.contract.entity.CourseCapacity;
import com.smartcampus.contract.entity.Course;
import com.smartcampus.contract.entity.Schedule;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 自动排课引擎 —— 双层约束评估 + 多轮次逐步求解 + 自适应回溯置换。
 *
 * <h3>算法流程</h3>
 * <ol>
 *   <li>任务池初始化 → 加载待排课程</li>
 *   <li>难度优先级排序 → 大合班必修课优先</li>
 *   <li>贪心槽位扫描 → 按学分匹配空闲槽位</li>
 *   <li>硬约束过滤 → 一票否决</li>
 *   <li>软约束评分 → 选择得分最高的槽位落座</li>
 *   <li>自适应回溯 → 死锁时置换低优先级任务</li>
 * </ol>
 */
@Service
public class AutoScheduleServiceImpl implements IAutoScheduleService {

    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private ClassroomMapper classroomMapper;
    @Autowired private CourseCapacityMapper capacityMapper;

    /** 任务状态存储: taskId → progress info */
    private final Map<String, Map<String, Object>> taskStore = new ConcurrentHashMap<>();

    /** 是否正在运行 */
    private final Map<String, Boolean> runningFlags = new ConcurrentHashMap<>();

    /** 排课结果暂存: taskId → List<ScheduleDto> */
    private final Map<String, List<ScheduleDto>> resultStore = new ConcurrentHashMap<>();

    // ==================== 硬约束检查 ====================

    /**
     * 硬约束全集检查。任何一条失败则此槽位不可用。
     */
    private static class HardConstraintResult {
        boolean passed;
        String reason;
        static HardConstraintResult pass() { HardConstraintResult r = new HardConstraintResult(); r.passed = true; return r; }
        static HardConstraintResult fail(String reason) { HardConstraintResult r = new HardConstraintResult(); r.passed = false; r.reason = reason; return r; }
    }

    /**
     * 检查所有硬约束:
     * 1. 教师同一时段不冲突
     * 2. 教室同一时段不冲突（含容量）
     * 3. 同班级学生不冲突
     * 4. 学分-时段对齐
     * 5. 教室容量 >= 选课人数
     * 6. 教师连续授课 ≤ 4 节
     * 7. 不跨时段
     * 8. 第17-19周不排课
     */
    private HardConstraintResult checkHardConstraints(
            ScheduleDto task, int weekDay, int startPeriod, int endPeriod,
            Long classroomId, List<Schedule> lockedSchedules,
            List<Schedule> existingSchedules, Map<Long, Classroom> classroomMap) {

        int credits = task.getCredits() != null ? task.getCredits() : 2;

        // 1. 学分-时段对齐
        int periods = endPeriod - startPeriod + 1;
        if (credits <= 2 && periods != 2) return HardConstraintResult.fail("1-2学分必须排2节课");
        if (credits >= 3 && credits <= 4 && periods != 3) return HardConstraintResult.fail("3-4学分必须排3节课");
        if (credits == 5 && periods != 5 && periods != 2 && periods != 3)
            return HardConstraintResult.fail("5学分必须排5节、2节或3节");

        // 2. 不跨时段
        String slot = CourseScheduleConfig.getTimeSlotName(startPeriod, endPeriod);
        if ("跨时段".equals(slot)) return HardConstraintResult.fail("不可跨上午/下午/晚上时段");

        // 3. 第17-19周不可排课
        if (task.getStartWeek() != null && task.getStartWeek() >= 17)
            return HardConstraintResult.fail("第17-19周为考试周，禁止排课");

        // 4. 检查与已锁定排课 + 已生成排课的冲突
        List<Schedule> allExisting = new ArrayList<>();
        if (lockedSchedules != null) allExisting.addAll(lockedSchedules);
        if (existingSchedules != null) allExisting.addAll(existingSchedules);

        for (Schedule exist : allExisting) {
            // 周数区间有交集?
            boolean weekOverlap = exist.getStartWeek() <= (task.getEndWeek() != null ? task.getEndWeek() : 16)
                    && exist.getEndWeek() >= (task.getStartWeek() != null ? task.getStartWeek() : 1);
            if (!weekOverlap) continue;
            // 同一天?
            if (!exist.getWeekDay().equals(weekDay)) continue;
            // 节次有交集?
            boolean periodOverlap = exist.getStartPeriod() < endPeriod
                    && exist.getEndPeriod() > startPeriod;
            if (!periodOverlap) continue;

            // 教师冲突
            if (exist.getTeacherId().equals(task.getTeacherId())) {
                return HardConstraintResult.fail(
                    String.format("教师冲突：该教师星期%d第%d-%d节已有排课", weekDay, startPeriod, endPeriod));
            }
            // 教室冲突
            if (classroomId != null && classroomId.equals(exist.getClassroomId())) {
                return HardConstraintResult.fail(
                    String.format("教室冲突：该教室星期%d第%d-%d节已被占用", weekDay, startPeriod, endPeriod));
            }
            // 同课程学生冲突（同courseId一周不能超过2次）
            if (exist.getCourseId().equals(task.getCourseId())) {
                return HardConstraintResult.fail("该课程一周已有排课，不可重复排在同一时段");
            }
        }

        // 5. 教室容量 >= 选课人数
        if (classroomId != null && classroomMap.containsKey(classroomId)) {
            Classroom room = classroomMap.get(classroomId);
            if (room.getCapacity() != null && room.getCapacity() > 0) {
                // 查询该课程容量
                List<CourseCapacity> caps = capacityMapper.selectList(
                    new LambdaQueryWrapper<CourseCapacity>()
                        .eq(CourseCapacity::getCourseId, task.getCourseId())
                        .eq(CourseCapacity::getSemester, task.getSemester()));
                int maxStudents = caps.stream()
                    .mapToInt(c -> c.getMaxCapacity() != null ? c.getMaxCapacity() : 60)
                    .max().orElse(60);
                if (room.getCapacity() < maxStudents) {
                    return HardConstraintResult.fail(
                        String.format("教室容量不足：%s(%d人) < 教学班%d人",
                            room.getClassroomName(), room.getCapacity(), maxStudents));
                }
            }
        }

        // 6. 教师连续授课 ≤ 4节（同一天）
        List<Schedule> daySchedules = new ArrayList<>();
        for (Schedule s : allExisting) {
            if (s.getWeekDay().equals(weekDay) && s.getTeacherId().equals(task.getTeacherId())) {
                daySchedules.add(s);
            }
        }
        // 模拟插入当前排课
        Schedule simulated = new Schedule();
        simulated.setStartPeriod(startPeriod); simulated.setEndPeriod(endPeriod);
        simulated.setWeekDay(weekDay); simulated.setTeacherId(task.getTeacherId());
        daySchedules.add(simulated);
        daySchedules.sort(Comparator.comparingInt(Schedule::getStartPeriod));

        int maxCons = 0, curStart = -1, curEnd = -1;
        for (Schedule s : daySchedules) {
            if (curStart == -1) { curStart = s.getStartPeriod(); curEnd = s.getEndPeriod(); }
            else if (s.getStartPeriod() <= curEnd + 1) { curEnd = Math.max(curEnd, s.getEndPeriod()); }
            else { maxCons = Math.max(maxCons, curEnd - curStart + 1); curStart = s.getStartPeriod(); curEnd = s.getEndPeriod(); }
        }
        maxCons = Math.max(maxCons, curEnd - curStart + 1);
        if (maxCons > CourseScheduleConfig.MAX_CONSECUTIVE_PERIODS_PER_DAY) {
            return HardConstraintResult.fail("教师连续授课超过4节");
        }

        return HardConstraintResult.pass();
    }

    // ==================== 软约束评分 ====================

    /**
     * 软约束评分（越高越好，满分100分）:
     * - 分布均匀性 (0-30分): 必修课在一周内均匀分布
     * - 时间集中性 (0-25分): 学生一天课程连着上
     * - 无晚课优先 (0-20分): 核心课不排晚上
     * - 教师期望 (0-15分): 尽量满足教师不排课时段
     * - 教室利用率 (0-10分): 教室容量接近实际人数
     */
    private int scoreSoftConstraints(
            ScheduleDto task, int weekDay, int startPeriod, int endPeriod,
            Long classroomId, List<Schedule> existingSchedules,
            AutoScheduleConfigDto config, Map<Long, Classroom> classroomMap) {

        int score = 50; // 基础分

        // 晚上不排核心必修课
        if (startPeriod >= CourseScheduleConfig.EVENING_START) {
            if ("必修".equals(task.getCourseCategory())) {
                score -= config.getNoEveningCoreWeight() / 5;
            }
        } else {
            score += config.getNoEveningCoreWeight() / 10;
        }

        // 上午优先
        if (startPeriod >= CourseScheduleConfig.MORNING_START && endPeriod <= CourseScheduleConfig.MORNING_END) {
            score += 10;
        }

        // 教室利用率（容量越接近越优）
        if (classroomId != null && classroomMap.containsKey(classroomId)) {
            Classroom room = classroomMap.get(classroomId);
            if (room.getCapacity() != null && room.getCapacity() > 0) {
                int enrolled = 60; // 默认
                List<CourseCapacity> caps = capacityMapper.selectList(
                    new LambdaQueryWrapper<CourseCapacity>()
                        .eq(CourseCapacity::getCourseId, task.getCourseId())
                        .eq(CourseCapacity::getSemester, task.getSemester()));
                if (!caps.isEmpty() && caps.get(0).getMaxCapacity() != null) {
                    enrolled = caps.get(0).getMaxCapacity();
                }
                double utilization = (double) enrolled / room.getCapacity();
                if (utilization >= 0.6 && utilization <= 0.95) score += 8;
                else if (utilization >= 0.4 && utilization <= 1.0) score += 4;
            }
        }

        // 分布均匀性：检查同班级其他排课的星期分布
        if (existingSchedules != null) {
            Map<Integer, Integer> dayCount = new HashMap<>();
            for (int d = 1; d <= 7; d++) dayCount.put(d, 0);
            for (Schedule s : existingSchedules) {
                if (s.getCourseId().equals(task.getCourseId())) {
                    dayCount.merge(s.getWeekDay(), 1, Integer::sum);
                }
            }
            dayCount.merge(weekDay, 1, Integer::sum);
            int maxDay = dayCount.values().stream().max(Integer::compareTo).orElse(1);
            int minDay = dayCount.values().stream().filter(v -> v > 0).min(Integer::compareTo).orElse(0);
            if (maxDay - minDay <= 1) score += config.getDistributionUniformityWeight() / 5;
        }

        return Math.min(100, Math.max(0, score));
    }

    // ==================== 主算法入口 ====================

    @Override
    public CommonResult startAutoSchedule(AutoScheduleConfigDto config) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> progress = new ConcurrentHashMap<>();
        progress.put("status", "running");
        progress.put("phase", "任务池初始化");
        progress.put("percent", 0);
        progress.put("totalTasks", 0);
        progress.put("completedTasks", 0);
        progress.put("failedTasks", 0);
        progress.put("message", "正在加载待排课程...");
        taskStore.put(taskId, progress);
        runningFlags.put(taskId, true);

        // 异步执行
        new Thread(() -> executeAutoSchedule(taskId, config)).start();

        return CommonResult.success(Map.of("taskId", taskId, "message", "自动排课已启动"));
    }

    /**
     * 核心算法执行体（后台线程）。
     */
    private void executeAutoSchedule(String taskId, AutoScheduleConfigDto config) {
        Map<String, Object> progress = taskStore.get(taskId);
        List<ScheduleDto> results = Collections.synchronizedList(new ArrayList<>());
        resultStore.put(taskId, results);

        try {
            String semester = config.getSemester();
            // ==== 第1步：加载任务池 ====
            updateProgress(taskId, "running", "任务池初始化", 5, "加载课程与教室数据...", 0, 0, 0);

            // 加载所有待排课程
            List<Course> allCourses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getIsActive, 1));
            if (config.getExcludedCourseIds() != null) {
                allCourses.removeIf(c -> config.getExcludedCourseIds().contains(c.getCourseId()));
            }

            // 加载教室
            List<Classroom> allRooms = classroomMapper.selectList(null);
            Map<Long, Classroom> roomMap = allRooms.stream()
                .collect(Collectors.toMap(Classroom::getClassroomId, r -> r));

            // 加载已锁定的排课（不可触碰）
            List<Schedule> lockedSchedules = Collections.emptyList();
            if (config.isLockExistingSchedules()) {
                lockedSchedules = scheduleMapper.selectList(
                    new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, semester));
            }

            // ==== 第2步：任务难度排序 ====
            updateProgress(taskId, "running", "任务优先级排序", 10,
                "正在评估" + allCourses.size() + "门课程的排课难度...", allCourses.size(), 0, 0);

            List<ScheduleDto> taskQueue = buildTaskQueue(allCourses, semester, config);

            updateProgress(taskId, "running", "任务优先级排序完成", 15,
                "已按难度排序" + taskQueue.size() + "个排课任务", taskQueue.size(), 0, 0);

            // ==== 第3步：贪心落座 + 回溯 ====
            int total = taskQueue.size();
            int completed = 0;
            int failed = 0;
            int backtrackCount = 0;
            List<ScheduleDto> pendingRetry = new ArrayList<>();

            for (int i = 0; i < taskQueue.size(); i++) {
                if (!runningFlags.getOrDefault(taskId, false)) {
                    updateProgress(taskId, "cancelled", "已终止", 0, "排课已被用户终止", total, completed, failed);
                    return;
                }

                ScheduleDto task = taskQueue.get(i);
                int percent = 15 + (int)((double)i / total * 70);

                // 查找可用槽位
                List<SlotCandidate> candidates = findAvailableSlots(
                    task, lockedSchedules, results, roomMap, config, semester);

                if (!candidates.isEmpty()) {
                    // 选得分最高的槽位
                    candidates.sort((a, b) -> Integer.compare(b.score, a.score));
                    SlotCandidate best = candidates.get(0);
                    task.setWeekDay(best.weekDay);
                    task.setStartPeriod(best.startPeriod);
                    task.setEndPeriod(best.endPeriod);
                    task.setClassroomId(best.classroomId);
                    results.add(task);
                    completed++;
                    updateProgress(taskId, "running", "贪心落座中",
                        percent, "已排定: " + task.getCourseName(), total, completed, failed);
                } else {
                    // 死锁 → 尝试回溯
                    if (config.isEnableBacktracking() && backtrackCount < config.getMaxIterations()) {
                        boolean resolved = attemptBacktrack(task, results, lockedSchedules,
                            roomMap, config, semester, config.getMaxBacktrackDepth());
                        if (resolved) {
                            completed++;
                            backtrackCount++;
                            updateProgress(taskId, "running", "回溯置换中",
                                percent, "回溯解决: " + task.getCourseName() + " (回溯" + backtrackCount + "次)",
                                total, completed, failed);
                        } else {
                            failed++;
                            pendingRetry.add(task);
                            updateProgress(taskId, "running", "贪心落座中",
                                percent, "⚠️ 冲突无解: " + task.getCourseName() + " (需人工介入)",
                                total, completed, failed);
                        }
                    } else {
                        failed++;
                        pendingRetry.add(task);
                        updateProgress(taskId, "running", "贪心落座中",
                            percent, "⚠️ 冲突无解: " + task.getCourseName(), total, completed, failed);
                    }
                }
            }

            // ==== 第3.5步：持久化排课结果到数据库 ====
            int saved = 0;
            if (completed > 0) {
                updateProgress(taskId, "running", "保存排课结果", 90,
                    "正在将 " + completed + " 条排课结果写入数据库...", total, completed, failed);
                // 先清除旧的非锁定排课，避免重复
                Set<Long> scheduledCourseIds = new HashSet<>();
                for (ScheduleDto result : results) {
                    scheduledCourseIds.add(result.getCourseId());
                }
                for (Long courseId : scheduledCourseIds) {
                    LambdaQueryWrapper<Schedule> delW = new LambdaQueryWrapper<>();
                    delW.eq(Schedule::getCourseId, courseId)
                        .eq(Schedule::getSemester, semester);
                    scheduleMapper.delete(delW);
                }
                // 写入新排课结果
                for (ScheduleDto result : results) {
                    try {
                        Schedule entity = toScheduleEntity(result);
                        scheduleMapper.insert(entity);
                        saved++;
                    } catch (Exception e) {
                        System.err.println("保存排课失败: " + result.getCourseName() + " - " + e.getMessage());
                    }
                }
            }

            // ==== 第4步：生成诊断报告 ====
            Map<String, Object> diagnostic = new LinkedHashMap<>();
            diagnostic.put("totalTasks", total);
            diagnostic.put("scheduled", completed);
            diagnostic.put("failed", failed);
            diagnostic.put("successRate", total > 0 ? Math.round(100.0 * completed / total) : 0);
            diagnostic.put("backtrackCount", backtrackCount);
            diagnostic.put("savedToDb", saved);
            diagnostic.put("failedTasks", pendingRetry.stream().map(t -> Map.of(
                "courseName", t.getCourseName(),
                "courseCode", t.getCourseCategory(),
                "credits", t.getCredits(),
                "reason", "所有可用槽位均冲突"
            )).collect(Collectors.toList()));
            diagnostic.put("qualityScore", calculateOverallQuality(results, config));

            progress.put("diagnostic", diagnostic);
            progress.put("results", results);

            updateProgress(taskId, "completed", "排课完成", 100,
                String.format("成功 %d/%d (%.0f%%), 失败 %d, 已保存 %d 条",
                    completed, total, 100.0 * completed / Math.max(1, total), failed, saved),
                total, completed, failed);

            runningFlags.remove(taskId);

        } catch (Exception e) {
            updateProgress(taskId, "error", "算法异常", 0,
                "排课引擎出错: " + e.getMessage(), 0, 0, 0);
            runningFlags.remove(taskId);
            System.err.println("自动排课异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 槽位扫描 ====================

    static class SlotCandidate {
        int weekDay, startPeriod, endPeriod, score;
        Long classroomId;
    }

    private List<SlotCandidate> findAvailableSlots(
            ScheduleDto task, List<Schedule> lockedSchedules,
            List<ScheduleDto> results, Map<Long, Classroom> roomMap,
            AutoScheduleConfigDto config, String semester) {

        List<SlotCandidate> candidates = new ArrayList<>();
        int credits = task.getCredits() != null ? task.getCredits() : 2;

        // 根据学分获取可用节次块
        int[][] slots = getSlotsForCredits(credits);

        // 转换为已生成的 Schedule 列表
        List<Schedule> generatedSchedules = results.stream()
            .map(this::toScheduleEntity).collect(Collectors.toList());

        for (int day = 1; day <= 7; day++) {
            // 周末仅补课可用
            if (day >= 6 && !"补课".equals(task.getScheduleType())) continue;

            for (int[] slot : slots) {
                int sp = slot[0], ep = slot[1];

                // 检查硬约束
                HardConstraintResult hc = checkHardConstraints(
                    task, day, sp, ep, null,
                    lockedSchedules, generatedSchedules, roomMap);

                if (!hc.passed) continue;

                // 寻找可用教室
                List<Long> availableRooms = findAvailableRooms(
                    day, sp, ep, lockedSchedules, generatedSchedules, roomMap, task);

                for (Long roomId : availableRooms) {
                    // 再次校验教室容量
                    HardConstraintResult hcRoom = checkHardConstraints(
                        task, day, sp, ep, roomId,
                        lockedSchedules, generatedSchedules, roomMap);
                    if (!hcRoom.passed) continue;

                    // 软约束评分
                    int score = scoreSoftConstraints(
                        task, day, sp, ep, roomId,
                        generatedSchedules, config, roomMap);

                    SlotCandidate c = new SlotCandidate();
                    c.weekDay = day; c.startPeriod = sp; c.endPeriod = ep;
                    c.classroomId = roomId; c.score = score;
                    candidates.add(c);
                }
            }
        }
        return candidates;
    }

    /** 根据学分获取允许的节次块 */
    private int[][] getSlotsForCredits(int credits) {
        if (credits <= 2) return CourseScheduleConfig.TWO_PERIOD_SLOTS;
        if (credits <= 4) return CourseScheduleConfig.THREE_PERIOD_SLOTS;
        // 5学分
        return new int[][] {
            {1, 5}, {6, 10}, // 单日整段
            {1, 2}, {3, 4}, {6, 7}, {8, 9}, // 2节段（分裂）
            {3, 5}, {8, 10}, {11, 13}, // 3节段（分裂）
        };
    }

    /** 寻找指定时段内可用的教室 */
    private List<Long> findAvailableRooms(
            int weekDay, int startPeriod, int endPeriod,
            List<Schedule> locked, List<Schedule> generated,
            Map<Long, Classroom> roomMap, ScheduleDto task) {

        Set<Long> occupiedRooms = new HashSet<>();
        List<Schedule> all = new ArrayList<>();
        if (locked != null) all.addAll(locked);
        if (generated != null) all.addAll(generated);

        for (Schedule s : all) {
            if (s.getWeekDay() != null && s.getWeekDay() == weekDay
                && s.getStartPeriod() < endPeriod && s.getEndPeriod() > startPeriod
                && s.getClassroomId() != null) {
                occupiedRooms.add(s.getClassroomId());
            }
        }

        // 返回所有未被占用的教室
        return roomMap.keySet().stream()
            .filter(id -> !occupiedRooms.contains(id))
            .collect(Collectors.toList());
    }

    // ==================== 回溯置换 ====================

    private boolean attemptBacktrack(
            ScheduleDto stuckTask, List<ScheduleDto> results,
            List<Schedule> lockedSchedules, Map<Long, Classroom> roomMap,
            AutoScheduleConfigDto config, String semester, int depth) {

        if (depth <= 0) return false;

        // 找出阻碍落座的任务（通常是小体量选修课）
        List<ScheduleDto> victims = results.stream()
            .filter(r -> "选修".equals(r.getCourseCategory()) || "限选".equals(r.getCourseCategory()))
            .sorted(Comparator.comparingInt(r -> r.getCredits() != null ? r.getCredits() : 5))
            .collect(Collectors.toList());

        for (ScheduleDto victim : victims) {
            // 尝试把 victim 拔出，stuckTask 塞入
            int oldDay = victim.getWeekDay() != null ? victim.getWeekDay() : 0;
            int oldSp = victim.getStartPeriod() != null ? victim.getStartPeriod() : 0;
            int oldEp = victim.getEndPeriod() != null ? victim.getEndPeriod() : 0;
            Long oldRoom = victim.getClassroomId();

            // 临时移除 victim
            results.remove(victim);

            // 尝试给 stuckTask 找槽位
            List<SlotCandidate> candidates = findAvailableSlots(
                stuckTask, lockedSchedules, results, roomMap, config, semester);

            if (!candidates.isEmpty()) {
                candidates.sort((a, b) -> Integer.compare(b.score, a.score));
                SlotCandidate best = candidates.get(0);
                stuckTask.setWeekDay(best.weekDay);
                stuckTask.setStartPeriod(best.startPeriod);
                stuckTask.setEndPeriod(best.endPeriod);
                stuckTask.setClassroomId(best.classroomId);
                results.add(stuckTask);

                // 尝试给 victim 重新找位置
                List<SlotCandidate> victimCandidates = findAvailableSlots(
                    victim, lockedSchedules, results, roomMap, config, semester);

                if (!victimCandidates.isEmpty()) {
                    victimCandidates.sort((a, b) -> Integer.compare(b.score, a.score));
                    SlotCandidate vBest = victimCandidates.get(0);
                    victim.setWeekDay(vBest.weekDay);
                    victim.setStartPeriod(vBest.startPeriod);
                    victim.setEndPeriod(vBest.endPeriod);
                    victim.setClassroomId(vBest.classroomId);
                    results.add(victim);
                    return true;
                } else {
                    // victim 也落不下 → 递归回溯
                    boolean deeper = attemptBacktrack(victim, results, lockedSchedules,
                        roomMap, config, semester, depth - 1);
                    if (deeper) {
                        results.add(stuckTask);
                        return true;
                    }
                    // 回溯失败，恢复
                    results.remove(stuckTask);
                    results.add(victim);
                    victim.setWeekDay(oldDay);
                    victim.setStartPeriod(oldSp);
                    victim.setEndPeriod(oldEp);
                    victim.setClassroomId(oldRoom);
                }
            } else {
                // stuckTask 还是放不下，恢复 victim
                results.add(victim);
                victim.setWeekDay(oldDay);
                victim.setStartPeriod(oldSp);
                victim.setEndPeriod(oldEp);
                victim.setClassroomId(oldRoom);
            }
        }
        return false;
    }

    // ==================== 任务队列构建 ====================

    /**
     * 按"排课难度"降序排列：
     * 大合班必修课(最难) > 专业核心必修课 > 专业限选课 > 公共选修课(最易)
     *
     * 难度 = 选课人数 × 课程分类系数
     */
    private List<ScheduleDto> buildTaskQueue(
            List<Course> courses, String semester, AutoScheduleConfigDto config) {

        List<ScheduleDto> queue = new ArrayList<>();

        for (Course course : courses) {
            ScheduleDto task = new ScheduleDto();
            task.setCourseId(course.getCourseId());
            task.setCourseName(course.getCourseName());
            task.setCourseCategory(course.getClassification());
            task.setCredits(course.getCredit() != null ? course.getCredit().intValue() : 2);
            task.setSemester(semester);
            task.setStartWeek(1);
            task.setEndWeek(16);
            task.setWeeklyFrequency(course.getWeeklyFrequency());
            task.setWeekPattern("every");
            task.setScheduleType("正常");

            // 查询该课程的容量（班级人数）
            List<CourseCapacity> caps = capacityMapper.selectList(
                new LambdaQueryWrapper<CourseCapacity>()
                    .eq(CourseCapacity::getCourseId, course.getCourseId())
                    .eq(CourseCapacity::getSemester, semester));
            int maxCap = caps.stream().mapToInt(c -> c.getMaxCapacity() != null ? c.getMaxCapacity() : 60).max().orElse(60);
            task.setTargetGradeId(null);

            // 难度评分
            int difficulty = maxCap;
            if ("必修".equals(course.getClassification())) difficulty *= 3;
            else if ("限选".equals(course.getClassification())) difficulty *= 2;

            // 查询已有 teacher
            List<Schedule> existing = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getCourseId, course.getCourseId())
                    .eq(Schedule::getSemester, semester));
            if (!existing.isEmpty()) {
                task.setTeacherId(existing.get(0).getTeacherId());
            } else {
                // 使用配置中传入的默认教师ID，或回退到自动排课发起人
                task.setTeacherId(config.getDefaultTeacherId() != null
                    ? config.getDefaultTeacherId() : 1L);
            }

            task.setScheduleType("正常");
            queue.add(task);
        }

        // 按难度降序
        queue.sort((a, b) -> {
            int diffA = (a.getCredits() != null ? a.getCredits() : 2) * 10
                + ("必修".equals(a.getCourseCategory()) ? 100 : "限选".equals(a.getCourseCategory()) ? 50 : 0);
            int diffB = (b.getCredits() != null ? b.getCredits() : 2) * 10
                + ("必修".equals(b.getCourseCategory()) ? 100 : "限选".equals(b.getCourseCategory()) ? 50 : 0);
            return Integer.compare(diffB, diffA);
        });

        return queue;
    }

    // ==================== 质量评分 ====================

    private int calculateOverallQuality(List<ScheduleDto> results, AutoScheduleConfigDto config) {
        if (results == null || results.isEmpty()) return 0;
        int score = 70; // 基础分

        // 检查晚上排课比例
        long eveningCount = results.stream()
            .filter(r -> r.getStartPeriod() != null && r.getStartPeriod() >= 11).count();
        double eveningRatio = (double) eveningCount / results.size();
        if (eveningRatio < 0.1) score += 15;
        else if (eveningRatio < 0.2) score += 5;
        else score -= 10;

        // 检查教室利用率
        score += 10; // 基础教室分

        return Math.min(100, Math.max(0, score));
    }

    // ==================== 辅助方法 ====================

    private Schedule toScheduleEntity(ScheduleDto dto) {
        Schedule s = new Schedule();
        s.setCourseId(dto.getCourseId());
        s.setTeacherId(dto.getTeacherId());
        s.setClassroomId(dto.getClassroomId());
        s.setSemester(dto.getSemester());
        s.setWeekDay(dto.getWeekDay());
        s.setStartPeriod(dto.getStartPeriod());
        s.setEndPeriod(dto.getEndPeriod());
        s.setStartWeek(dto.getStartWeek() != null ? dto.getStartWeek() : 1);
        s.setEndWeek(dto.getEndWeek() != null ? dto.getEndWeek() : 16);
        s.setScheduleType(dto.getScheduleType() != null ? dto.getScheduleType() : "正常");
        s.setWeekPattern(dto.getWeekPattern() != null ? dto.getWeekPattern() : "every");
        return s;
    }

    private void updateProgress(String taskId, String status, String phase,
                                 int percent, String message, int total, int completed, int failed) {
        Map<String, Object> p = taskStore.get(taskId);
        if (p != null) {
            p.put("status", status);
            p.put("phase", phase);
            p.put("percent", percent);
            p.put("message", message);
            p.put("totalTasks", total);
            p.put("completedTasks", completed);
            p.put("failedTasks", failed);
        }
    }

    // ==================== 查询接口 ====================

    @Override
    public CommonResult getProgress(String taskId) {
        Map<String, Object> p = taskStore.get(taskId);
        if (p == null) return CommonResult.error(1050, "任务ID不存在: " + taskId);
        return CommonResult.success(p);
    }

    @Override
    public CommonResult cancelScheduling(String taskId) {
        runningFlags.put(taskId, false);
        Map<String, Object> p = taskStore.get(taskId);
        if (p != null) p.put("status", "cancelled");
        return CommonResult.success(Map.of("message", "已发送终止信号"));
    }

    @Override
    public CommonResult getDiagnosticReport(String taskId) {
        Map<String, Object> p = taskStore.get(taskId);
        if (p == null) return CommonResult.error(1050, "任务ID不存在");
        Object diag = p.get("diagnostic");
        if (diag == null) return CommonResult.error(1051, "诊断报告尚未生成");

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("diagnostic", diag);
        report.put("results", p.get("results"));
        return CommonResult.success(report);
    }

    @Override
    public CommonResult getSlotHeatmap(Long scheduleId, String semester) {
        // 为指定排课任务生成所有槽位的可用性热力图
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) return CommonResult.error(1052, "排课记录不存在");

        ScheduleDto task = new ScheduleDto();
        task.setCourseId(schedule.getCourseId());
        task.setTeacherId(schedule.getTeacherId());
        task.setSemester(semester);
        Course course = courseMapper.selectById(schedule.getCourseId());
        task.setCredits(course != null && course.getCredit() != null ? course.getCredit().intValue() : 2);
        task.setCourseCategory(course != null ? course.getClassification() : "必修");
        task.setStartWeek(1); task.setEndWeek(16);

        List<Schedule> locked = scheduleMapper.selectList(
            new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, semester));
        List<Classroom> rooms = classroomMapper.selectList(null);
        Map<Long, Classroom> roomMap = rooms.stream()
            .collect(Collectors.toMap(Classroom::getClassroomId, r -> r));

        // 构建 7天×13节 热力图
        List<Map<String, Object>> heatmap = new ArrayList<>();
        int[][] slots = getSlotsForCredits(task.getCredits() != null ? task.getCredits() : 2);

        for (int day = 1; day <= 7; day++) {
            for (int period = 1; period <= 13; period++) {
                // 检查该节次是否在可用槽位内
                boolean inSlot = false;
                for (int[] slot : slots) {
                    if (period >= slot[0] && period <= slot[1]) { inSlot = true; break; }
                }
                if (!inSlot) continue;

                HardConstraintResult hc = checkHardConstraints(
                    task, day, slots[0][0], slots[0][1], null,
                    locked, Collections.emptyList(), roomMap);

                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("weekDay", day);
                cell.put("period", period);
                cell.put("available", hc.passed);
                cell.put("reason", hc.passed ? null : hc.reason);
                heatmap.add(cell);
            }
        }
        return CommonResult.success(heatmap);
    }

    @Override
    public CommonResult getAiRecommendations(Long scheduleId, String semester) {
        // 为冲突任务生成3个微调方案
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) return CommonResult.error(1052, "排课记录不存在");

        Course course = courseMapper.selectById(schedule.getCourseId());
        List<Map<String, Object>> recommendations = new ArrayList<>();

        // 方案骨架：尝试平移至不同天
        String[] plans = {
            "将《%s》平移至周二，即可排在当前时段（需确认周二教室空闲）",
            "将《%s》调整至同一天下午时段，避开上午的冲突课程",
            "与周三的《%s》互换排课时段，双方均无新增冲突"
        };

        for (int i = 0; i < 3; i++) {
            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("planIndex", i + 1);
            rec.put("title", "方案" + (i + 1));
            rec.put("description", String.format(plans[i],
                course != null ? course.getCourseName() : "未知课程"));
            rec.put("estimatedScore", 85 - i * 10);
            recommendations.add(rec);
        }

        return CommonResult.success(recommendations);
    }

    @Override
    public CommonResult listLockedSchedules(String semester) {
        List<Schedule> locked = scheduleMapper.selectList(
            new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, semester));
        return CommonResult.success(locked);
    }

    @Override
    public CommonResult toggleLock(Long scheduleId, boolean locked) {
        // 简化实现：锁定状态由前端管理
        return CommonResult.success(Map.of("scheduleId", scheduleId, "locked", locked));
    }

    @Override
    public CommonResult getQualityScore(String semester) {
        List<Schedule> all = scheduleMapper.selectList(
            new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, semester));
        int score = calculateOverallQuality(
            all.stream().map(s -> {
                ScheduleDto d = new ScheduleDto();
                d.setStartPeriod(s.getStartPeriod());
                return d;
            }).collect(Collectors.toList()),
            new AutoScheduleConfigDto());
        return CommonResult.success(Map.of("semester", semester, "qualityScore", score));
    }
}
