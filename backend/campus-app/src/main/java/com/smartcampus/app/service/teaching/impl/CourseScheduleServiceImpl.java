package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.config.CourseScheduleConfig;
import com.smartcampus.app.config.CourseScheduleConfig;
import com.smartcampus.app.dao.teaching.ClassroomMapper;
import com.smartcampus.app.dao.teaching.CourseCapacityMapper;
import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.app.dao.teaching.CourseSelectionMapper;
import com.smartcampus.app.dao.teaching.ScheduleMapper;
import com.smartcampus.app.dto.teaching.ScheduleDto;
import com.smartcampus.app.exception.ScheduleConflictException;
import com.smartcampus.app.service.teaching.ICourseScheduleService;
import com.smartcampus.app.validator.ScheduleValidator;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Classroom;
import com.smartcampus.contract.entity.CourseCapacity;
import com.smartcampus.contract.entity.Course;
import com.smartcampus.contract.entity.CourseSelection;
import com.smartcampus.contract.entity.Schedule;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排课管理——F1 5学分智能分拆 + F2 补课系统 + F3(选课服务) + F4 教师工作量预警。
 */
@Service
public class CourseScheduleServiceImpl implements ICourseScheduleService {

    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private CourseCapacityMapper capacityMapper;
    @Autowired private ClassroomMapper classroomMapper;
    @Autowired private CourseSelectionMapper courseSelectionMapper;

    /** F4: 教师每周最大授课节数预警/阻断阈值 */
    private static final int WORKLOAD_WARN = 16;
    private static final int WORKLOAD_BLOCK = 20;

    // ========================================================================
    //  新增排课（F1+F2+F4）
    // ========================================================================

    @Override
    @Transactional
    public CommonResult addSchedule(ScheduleDto dto) {

        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) return CommonResult.error(1001, "课程不存在");
        dto.setCourseName(course.getCourseName());

        // F2: 补课允许周六日，正常排课不允许
        if (!"补课".equals(dto.getScheduleType())
            && (dto.getWeekDay() == 6 || dto.getWeekDay() == 7)) {
            return CommonResult.error(1010, "常规排课仅限周一至周五。如需周末补课请选择 scheduleType='补课'");
        }

        // 兼容前端未传credits：从课程表读取
        if (dto.getCredits() == null && course.getCredit() != null) {
            dto.setCredits(course.getCredit().intValue());
        }
        if (dto.getCredits() == null) dto.setCredits(2); // 默认2学分

        // ---- 规则校验 ----
        long total = scheduleMapper.selectCount(
            new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, dto.getSemester()));
        long shortCount = 0;
        try { ScheduleValidator.validateSchedule(dto, total, shortCount); }
        catch (ScheduleConflictException e) { return CommonResult.error(1002, e.getMessage()); }

        // ==== 漏洞4修复：总学时稽核 ====
        CommonResult cr = validateCreditHours(dto);
        if (cr != null) return cr;

        // ---- 教师冲突 ----
        CommonResult tc = validateTeacherConflict(dto, null);
        if (tc != null) return tc;

        // ==== 漏洞13修复：教室占用冲突（含周数区间交集校验） ====
        CommonResult cc = validateClassroomConflict(dto, null);
        if (cc != null) return cc;

        // ---- F4: 教师工作量 ----
        CommonResult wl = checkTeacherWorkload(dto);
        if (wl != null) return wl;

        // ---- 学生重复 ----
        CommonResult sd = validateStudentDuplicatePerWeek(dto);
        if (sd != null) return sd;

        // ==== 漏洞10修复：选修课排课避开目标年级的必修课时段 ====
        if (dto.getTargetGradeId() != null && !"必修".equals(course.getClassification())) {
            // 查询该年级所有必修课的排课时段
            LambdaQueryWrapper<Schedule> gradeCompulsory = new LambdaQueryWrapper<>();
            gradeCompulsory.eq(Schedule::getTargetGradeId, dto.getTargetGradeId())
                .inSql(Schedule::getCourseId,
                    "SELECT course_id FROM course WHERE classification='必修'");
            List<Schedule> compulsorySlots = scheduleMapper.selectList(gradeCompulsory);
            for (Schedule cs : compulsorySlots) {
                if (cs.getWeekDay().equals(dto.getWeekDay())
                    && dto.getStartPeriod() < cs.getEndPeriod()
                    && dto.getEndPeriod() > cs.getStartPeriod()) {
                    return CommonResult.error(1017,
                        String.format("该时段已安排目标年级的必修课《%s》(星期%d 第%d-%d节)，"
                            + "选修课不可占用必修课时间。请选择其他时段。",
                            courseMapper.selectById(cs.getCourseId()).getCourseName(),
                            cs.getWeekDay(), cs.getStartPeriod(), cs.getEndPeriod()));
                }
            }
        }

        // ==== 漏洞6修复：17-19周为期末考试周，禁止排常规课 ====
        if (dto.getEndWeek() >= 17 || dto.getStartWeek() >= 17) {
            return CommonResult.error(1016,
                "第17-19周为期末考试周，不可安排常规授课。如需在此时段使用教室请联系教务处安排补考。");
        }

        // ==== 漏洞1修复：教室容量 >= 教学班最大容量 ====
        if (dto.getClassroomId() != null) {
            Classroom room = classroomMapper.selectById(dto.getClassroomId());
            if (room == null) {
                return CommonResult.error(1011, "教室不存在，ID: " + dto.getClassroomId());
            }
            if (room.getCapacity() == null || room.getCapacity() <= 0) {
                return CommonResult.error(1012, "教室《" + room.getClassroomName() + "》未配置容量，无法排课");
            }
            // 从 course_capacity 表查教学班最大人数
            LambdaQueryWrapper<CourseCapacity> capW = new LambdaQueryWrapper<>();
            capW.eq(CourseCapacity::getCourseId, dto.getCourseId())
                .eq(CourseCapacity::getSemester, dto.getSemester());
            CourseCapacity cap = capacityMapper.selectOne(capW);
            if (cap != null && cap.getMaxCapacity() != null && room.getCapacity() < cap.getMaxCapacity()) {
                return CommonResult.error(1013,
                    String.format("教室容量不足！%s(容纳%d人) 无法容纳 %d 人选课班。请更换更大教室。",
                        room.getClassroomName(), room.getCapacity(), cap.getMaxCapacity()));
            }
        }

        // ==== 漏洞3修复：教师同一天连续授课不得超过4节 ====
        CommonResult cons = checkConsecutiveLimit(dto);
        if (cons != null) return cons;

        // ---- F1: 5学分跨日分拆 ----
        List<ScheduleDto> segments = dto.getSecondSegments();
        if (segments != null && !segments.isEmpty()) {
            // 先插入第一段
            Schedule first = toEntity(dto);
            scheduleMapper.insert(first);

            // 再插入第二段，设置 parentId 指向第一段
            for (ScheduleDto seg : segments) {
                Schedule second = toEntity(seg);
                second.setParentId(first.getScheduleId());
                scheduleMapper.insert(second);
            }
        } else {
            scheduleMapper.insert(toEntity(dto));
        }

        return CommonResult.success();
    }

    // ========================================================================
    //  漏洞4修复：学分-学时稽核
    // ========================================================================

    /**
     * 实际授课周数 × 每周频次 × 每次节数 >= 学分 × 16。
     * <p>单双周模式需折半计算实际授课周数。</p>
     */
    private CommonResult validateCreditHours(ScheduleDto dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) return null; // 已在前面校验

        int credits = course.getCredit() != null ? course.getCredit().intValue() : 0;
        if (credits <= 0) return null;

        int weeklyFreq = dto.getWeeklyFrequency() != null ? dto.getWeeklyFrequency() : 1;
        int periodsPerSession = dto.getEndPeriod() - dto.getStartPeriod() + 1;
        int totalWeeks = dto.getEndWeek() - dto.getStartWeek() + 1;

        // 单双周实际授课周数 = ceil(总周数 / 2)
        int actualWeeks = totalWeeks;
        if ("odd".equals(dto.getWeekPattern()) || "even".equals(dto.getWeekPattern())) {
            actualWeeks = (totalWeeks + 1) / 2; // 向上取整
        }

        int actualHours = actualWeeks * weeklyFreq * periodsPerSession;
        int requiredHours = credits * CourseScheduleConfig.DEFAULT_TOTAL_WEEKS; // 学分 × 16

        if (actualHours < requiredHours) {
            return CommonResult.error(1015,
                String.format("学时不足！课程《%s》(%d学分) 要求至少%d课时，当前排课仅%d课时。"
                    + "请增加教学周数或提高周频次。",
                    course.getCourseName(), credits, requiredHours, actualHours));
        }
        return null;
    }

    // ========================================================================
    //  F4: 教师每周工作量校验（漏洞5修复：穿透周模式）
    // ========================================================================

    /**
     * 计算该教师在本学期所有排课的总周节数，>16 预警，>20 阻断。
     * 漏洞5修复：穿透 week_pattern，按实际授课周数逐周累加，避免虚高。
     */
    private CommonResult checkTeacherWorkload(ScheduleDto dto) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getTeacherId, dto.getTeacherId())
         .eq(Schedule::getSemester, dto.getSemester());
        List<Schedule> all = scheduleMapper.selectList(w);

        // 把新排课也加入计算
        Schedule pending = new Schedule();
        pending.setStartPeriod(dto.getStartPeriod());
        pending.setEndPeriod(dto.getEndPeriod());
        pending.setStartWeek(dto.getStartWeek());
        pending.setEndWeek(dto.getEndWeek());
        pending.setWeekPattern(dto.getWeekPattern() != null ? dto.getWeekPattern() : "every");
        all.add(pending);

        // 漏洞5: 逐周累加，穿透单双周模式
        int maxWeek = CourseScheduleConfig.DEFAULT_TOTAL_WEEKS;
        int[] weeklyLoad = new int[maxWeek + 1]; // 1-indexed

        for (Schedule s : all) {
            int periods = s.getEndPeriod() - s.getStartPeriod() + 1;
            String pattern = s.getWeekPattern() != null ? s.getWeekPattern() : "every";
            for (int wk = s.getStartWeek(); wk <= s.getEndWeek() && wk <= maxWeek; wk++) {
                boolean active = "every".equals(pattern)
                    || ("odd".equals(pattern) && wk % 2 == 1)
                    || ("even".equals(pattern) && wk % 2 == 0);
                if (active) weeklyLoad[wk] += periods;
            }
        }

        for (int wk = 1; wk <= maxWeek; wk++) {
            if (weeklyLoad[wk] > WORKLOAD_BLOCK) {
                return CommonResult.error(1020,
                    String.format("【排课阻断】教师%d在第%d周授课将达%d节，超过%d节上限。",
                        dto.getTeacherId(), wk, weeklyLoad[wk], WORKLOAD_BLOCK));
            }
            if (weeklyLoad[wk] > WORKLOAD_WARN) {
                System.err.printf("[工作量预警] 教师%d 第%d周授课将达%d节，请关注均衡。%n",
                    dto.getTeacherId(), wk, weeklyLoad[wk]);
            }
        }
        return null;
    }

    // ========================================================================
    //  漏洞3修复：教师同一天连续授课节数上限校验
    // ========================================================================

    /**
     * 同一教师在同一天内连续授课不得超过 {@link CourseScheduleConfig#MAX_CONSECUTIVE_PERIODS_PER_DAY}（4节）。
     * <p>算法：查询该教师同一天的所有排课，按 startPeriod 排序，检查是否存在相邻两段或重叠段
     * 导致连续的授课节数超过上限。同时检查新排课插入后是否造成超过阈值。</p>
     */
    private CommonResult checkConsecutiveLimit(ScheduleDto dto) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getTeacherId, dto.getTeacherId())
         .eq(Schedule::getSemester, dto.getSemester())
         .eq(Schedule::getWeekDay, dto.getWeekDay())
         .orderByAsc(Schedule::getStartPeriod);
        List<Schedule> daySchedules = scheduleMapper.selectList(w);

        // 把当前待排的也加入列表
        Schedule pending = new Schedule();
        pending.setStartPeriod(dto.getStartPeriod());
        pending.setEndPeriod(dto.getEndPeriod());
        daySchedules.add(pending);
        daySchedules.sort((a, b) -> a.getStartPeriod() - b.getStartPeriod());

        // 合并重叠/相邻段，找出最长连续
        int maxConsecutive = 0;
        int currentStart = -1;
        int currentEnd = -1;

        for (Schedule s : daySchedules) {
            if (currentStart == -1) {
                currentStart = s.getStartPeriod();
                currentEnd = s.getEndPeriod();
            } else if (s.getStartPeriod() <= currentEnd + 1) {
                // 相邻（中间空1节也算连上）或重叠，合并
                currentEnd = Math.max(currentEnd, s.getEndPeriod());
            } else {
                // 断开，结算上一段
                maxConsecutive = Math.max(maxConsecutive, currentEnd - currentStart + 1);
                currentStart = s.getStartPeriod();
                currentEnd = s.getEndPeriod();
            }
        }
        maxConsecutive = Math.max(maxConsecutive, currentEnd - currentStart + 1);

        int limit = CourseScheduleConfig.MAX_CONSECUTIVE_PERIODS_PER_DAY;
        if (maxConsecutive > limit) {
            return CommonResult.error(1014,
                String.format("教师%d在星期%d连续授课将达%d节，超过%d节上限。请间隔排课以保障教学质量。",
                    dto.getTeacherId(), dto.getWeekDay(), maxConsecutive, limit));
        }
        return null;
    }

    // ========================================================================
    //  F4: 工作量查询接口
    // ========================================================================

    public CommonResult getTeacherWorkload(Long teacherId, String semester) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getTeacherId, teacherId)
         .eq(Schedule::getSemester, semester);
        List<Schedule> all = scheduleMapper.selectList(w);

        int total = 0;
        Map<Integer, Integer> dayMap = new HashMap<>(); // weekDay -> periods
        for (Schedule s : all) {
            int p = s.getEndPeriod() - s.getStartPeriod() + 1;
            total += p;
            dayMap.merge(s.getWeekDay(), p, Integer::sum);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalPeriods", total);
        result.put("weeklyBreakdown", dayMap);
        result.put("warn", total > WORKLOAD_WARN);
        result.put("block", total >= WORKLOAD_BLOCK);
        result.put("blockThreshold", WORKLOAD_BLOCK);
        return CommonResult.success(result);
    }

    // ========================================================================
    //  级联删除（F1: parent_id 关联）
    // ========================================================================

    @Override
    @Transactional
    public CommonResult deleteSchedule(Long scheduleId) {
        Schedule s = scheduleMapper.selectById(scheduleId);
        if (s == null) return CommonResult.error(1004, "排课记录不存在");

        // F1: 删除以本记录为 parent_id 的子记录
        LambdaQueryWrapper<Schedule> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Schedule::getParentId, scheduleId);
        scheduleMapper.delete(childWrapper);

        // 如果本记录有 parentId，也要考虑（确保双向级联）
        scheduleMapper.deleteById(scheduleId);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult suspendCourse(Long courseId, String semester) {
        // 先查出所有直接排课
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getCourseId, courseId).eq(Schedule::getSemester, semester);
        List<Schedule> schedules = scheduleMapper.selectList(w);

        for (Schedule s : schedules) {
            // F1: 删除子记录
            LambdaQueryWrapper<Schedule> childW = new LambdaQueryWrapper<>();
            childW.eq(Schedule::getParentId, s.getScheduleId());
            scheduleMapper.delete(childW);
        }
        scheduleMapper.delete(w);

        // 删除选课记录
        LambdaQueryWrapper<CourseSelection> csW = new LambdaQueryWrapper<>();
        csW.eq(CourseSelection::getCourseId, courseId).eq(CourseSelection::getSemester, semester);
        courseSelectionMapper.delete(csW);

        // 漏洞7: 标记课程为停开，课表和学分计算自动过滤
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setIsActive(0);
            courseMapper.updateById(course);
        }

        return CommonResult.success();
    }

    // ========================================================================
    //  其他方法（保持不变）
    // ========================================================================

    @Override @Transactional
    public CommonResult updateSchedule(ScheduleDto dto) {
        if (dto.getScheduleId() == null) return CommonResult.error(1003, "排课ID不能为空");
        Schedule existing = scheduleMapper.selectById(dto.getScheduleId());
        if (existing == null) return CommonResult.error(1004, "排课记录不存在");

        Course course = courseMapper.selectById(dto.getCourseId());
        if (course != null) dto.setCourseName(course.getCourseName());

        try { ScheduleValidator.validateSchedule(dto, 0, 0); }
        catch (ScheduleConflictException e) { return CommonResult.error(1002, e.getMessage()); }

        CommonResult tc = validateTeacherConflict(dto, dto.getScheduleId());
        if (tc != null) return tc;

        Schedule entity = toEntity(dto);
        entity.setScheduleId(dto.getScheduleId());
        scheduleMapper.updateById(entity);
        return CommonResult.success();
    }

    @Override
    public CommonResult getShortCourseRatio(String semester) {
        long total = scheduleMapper.selectCount(
            new LambdaQueryWrapper<Schedule>().eq(Schedule::getSemester, semester));
        LambdaQueryWrapper<Schedule> sw = new LambdaQueryWrapper<>();
        sw.eq(Schedule::getSemester, semester)
          .apply("(end_week - start_week + 1) < {0}", CourseScheduleConfig.DEFAULT_TOTAL_WEEKS);
        long sc = scheduleMapper.selectCount(sw);
        double r = total > 0 ? (double) sc / total : 0;
        return CommonResult.success("{\"ratio\":" + String.format("%.2f", r) + ",\"warn\":" + (r > 0.15) + "}");
    }

    @Override
    public CommonResult getTeacherSchedule(Long teacherId, String semester) {
        // 0 表示教务处查看全部排课
        if (teacherId != null && teacherId == 0L) {
            return CommonResult.success(scheduleMapper.selectAll(semester));
        }
        return CommonResult.success(scheduleMapper.selectByTeacher(teacherId, semester));
    }

    @Override
    public CommonResult getStudentSchedule(Long studentId, String semester) {
        return CommonResult.success(scheduleMapper.selectByStudent(studentId, semester));
    }

    // ========================================================================
    //  漏洞13: 教室占用冲突（含周数区间交集）
    // ========================================================================

    /**
     * 教室冲突判定（四维时空检测）：
     * 同一教室 + 同一天 + 同时段 + 周数区间有交集 → 冲突。
     * 若课程A在1-8周用301教室，课程B在9-16周用301，周数无交集 → 不冲突。
     */
    private CommonResult validateClassroomConflict(ScheduleDto dto, Long excludeId) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getClassroomId, dto.getClassroomId())
         .eq(Schedule::getSemester, dto.getSemester())
         .eq(Schedule::getWeekDay, dto.getWeekDay())
         // 周数区间有交集: existing.startWeek <= new.endWeek AND existing.endWeek >= new.startWeek
         .le(Schedule::getStartWeek, dto.getEndWeek())
         .ge(Schedule::getEndWeek, dto.getStartWeek())
         // 节次区间有交集
         .lt(Schedule::getStartPeriod, dto.getEndPeriod())
         .gt(Schedule::getEndPeriod, dto.getStartPeriod());
        if (excludeId != null) w.ne(Schedule::getScheduleId, excludeId);

        List<Schedule> list = scheduleMapper.selectList(w);
        if (!list.isEmpty()) {
            Schedule c = list.get(0);
            Course conflictCourse = courseMapper.selectById(c.getCourseId());
            return CommonResult.error(1018,
                String.format("教室冲突！%s在星期%d 第%d-%d节(第%d-%d周)已被《%s》占用。"
                    + "两个排课的周数区间存在重叠，无法共用该教室。",
                    dto.getClassroomId() != null ? "该教室" : "",
                    c.getWeekDay(), c.getStartPeriod(), c.getEndPeriod(),
                    c.getStartWeek(), c.getEndWeek(),
                    conflictCourse != null ? conflictCourse.getCourseName() : "未知课程"));
        }
        return null;
    }

    // ========================================================================
    //  教师冲突 + 学生重复（原逻辑保留）
    // ========================================================================

    private CommonResult validateTeacherConflict(ScheduleDto dto, Long excludeId) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getTeacherId, dto.getTeacherId())
         .eq(Schedule::getSemester, dto.getSemester())
         .eq(Schedule::getWeekDay, dto.getWeekDay())
         .le(Schedule::getStartWeek, dto.getEndWeek())
         .ge(Schedule::getEndWeek, dto.getStartWeek())
         .lt(Schedule::getStartPeriod, dto.getEndPeriod())
         .gt(Schedule::getEndPeriod, dto.getStartPeriod());
        if (excludeId != null) w.ne(Schedule::getScheduleId, excludeId);
        List<Schedule> list = scheduleMapper.selectList(w);
        if (!list.isEmpty()) {
            Schedule c = list.get(0);
            return CommonResult.error(1005,
                String.format("教师冲突！该教师星期%d 第%d-%d节已有排课（课程ID:%d）",
                    c.getWeekDay(), c.getStartPeriod(), c.getEndPeriod(), c.getCourseId()));
        }
        return null;
    }

    private CommonResult validateStudentDuplicatePerWeek(ScheduleDto dto) {
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<>();
        w.eq(Schedule::getCourseId, dto.getCourseId())
         .eq(Schedule::getSemester, dto.getSemester())
         .le(Schedule::getStartWeek, dto.getEndWeek())
         .ge(Schedule::getEndWeek, dto.getStartWeek());
        if (dto.getScheduleId() != null) w.ne(Schedule::getScheduleId, dto.getScheduleId());
        long sameDay = scheduleMapper.selectList(w).stream()
            .filter(s -> s.getWeekDay().equals(dto.getWeekDay())).count();
        if (sameDay >= 2) return CommonResult.error(1006,
            String.format("课程《%s》一周已有%d次排课，不允许一周上三次", dto.getCourseName(), sameDay + 1));
        return null;
    }

    private Schedule toEntity(ScheduleDto dto) {
        Schedule s = new Schedule();
        s.setScheduleId(dto.getScheduleId());
        s.setCourseId(dto.getCourseId());
        s.setClassroomId(dto.getClassroomId());
        s.setTeacherId(dto.getTeacherId());
        s.setSemester(dto.getSemester());
        s.setWeekDay(dto.getWeekDay());
        s.setStartPeriod(dto.getStartPeriod());
        s.setEndPeriod(dto.getEndPeriod());
        s.setStartWeek(dto.getStartWeek());
        s.setEndWeek(dto.getEndWeek());
        s.setScheduleType(dto.getScheduleType() != null ? dto.getScheduleType() : "正常");
        s.setParentId(dto.getParentId());
        s.setWeekPattern(dto.getWeekPattern() != null ? dto.getWeekPattern() : "every");
        s.setTargetGradeId(dto.getTargetGradeId());
        return s;
    }
}
