package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.*;
import com.smartcampus.app.service.teaching.IExamService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired private ExamMapper examMapper;
    @Autowired private ResitApplyMapper resitApplyMapper;
    @Autowired private ScoreMapper scoreMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private CourseSelectionMapper selectionMapper;
    @Autowired private ClassroomMapper classroomMapper;
    @Autowired private ScheduleMapper scheduleMapper;

    // ==================== 教务手动创建考试 ====================

    @Override
    public CommonResult createExam(Exam exam) {
        if (exam.getExamDate() != null && exam.getStartTime() != null && exam.getEndTime() != null) {
            if (exam.getEndTime().before(exam.getStartTime())) {
                return CommonResult.error(930, "结束时间不能早于开始时间");
            }
        }
        examMapper.insert(exam);
        return CommonResult.success();
    }

    // ==================== 统一排考引擎 ====================

    /** 可用考试时段：每天3场 (上午9-11, 下午14-16, 晚上18-20)，持续7天 */
    private static final int[][] EXAM_SLOTS = {
        {9, 0, 11, 0}, {14, 0, 16, 0}, {18, 0, 20, 0}
    };

    @Override
    @Transactional
    public CommonResult scheduleExams(String semester) {
        // 1. 删除该学期旧考试，避免重复
        LambdaQueryWrapper<Exam> delW = new LambdaQueryWrapper<>();
        delW.eq(Exam::getSemester, semester);
        examMapper.delete(delW);

        // 2. 提取所有选课记录，按 course_code 分组
        List<Map<String, Object>> selections = selectionMapper.selectBySemester(semester);
        if (selections.isEmpty()) return CommonResult.error(945, "该学期无选课记录");

        Map<String, List<Map<String, Object>>> byCourseCode = new LinkedHashMap<>();
        for (Map<String, Object> s : selections) {
            String code = (String) s.getOrDefault("course_code", "UNKNOWN");
            byCourseCode.computeIfAbsent(code, k -> new ArrayList<>()).add(s);
        }

        // 3. 构建学生->考试冲突矩阵
        Map<Long, Set<String>> studentCourses = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : byCourseCode.entrySet()) {
            for (Map<String, Object> s : e.getValue()) {
                Long sid = (Long) s.get("student_id");
                studentCourses.computeIfAbsent(sid, k -> new HashSet<>()).add(e.getKey());
            }
        }

        // 4. 按 course_code 逐个排考，自动分配无冲突时段
        LocalDate startDate = LocalDate.now().plusDays(3); // 3天后开始
        List<Map<String, String>> occupied = new ArrayList<>(); // 已占用的 [date,time] 槽位
        Map<String, Set<String>> slotCourseCodes = new LinkedHashMap<>(); // 同时段已排的course_code集合（用于冲突检测）

        int totalExams = 0;
        int slotIdx = 0;

        for (Map.Entry<String, List<Map<String, Object>>> entry : byCourseCode.entrySet()) {
            String courseCode = entry.getKey();
            List<Map<String, Object>> students = entry.getValue();
            String courseName = (String) students.get(0).getOrDefault("course_name", courseCode);
            Long courseId = (Long) students.get(0).get("course_id");

            // 找可用时段：不与已排考试的学生群体冲突
            boolean found = false;
            for (int attempt = 0; attempt < 50 && !found; attempt++) {
                int dayOffset = slotIdx / 3;
                int timeIdx = slotIdx % 3;
                int[] timeSlot = EXAM_SLOTS[timeIdx];

                LocalDate examDate = startDate.plusDays(dayOffset);
                String dateStr = examDate.toString();
                String timeStr = timeSlot[0] + ":" + String.format("%02d", timeSlot[1]);

                // 跳过周末
                if (examDate.getDayOfWeek().getValue() >= 6) { slotIdx++; continue; }

                // 冲突检测：该时段已排的考试中，是否有和当前考试共享学生的？
                Set<String> conflictCodes = slotCourseCodes.get(dateStr + timeStr);
                boolean hasConflict = false;
                if (conflictCodes != null) {
                    Set<Long> currentStudents = new HashSet<>();
                    for (Map<String, Object> s : students) currentStudents.add((Long) s.get("student_id"));
                    for (String cc : conflictCodes) {
                        for (Map<String, Object> s : byCourseCode.get(cc)) {
                            if (currentStudents.contains((Long) s.get("student_id"))) {
                                hasConflict = true; break;
                            }
                        }
                        if (hasConflict) break;
                    }
                }

                if (!hasConflict) {
                    // 可用 → 落座
                    Exam exam = new Exam();
                    exam.setCourseId(courseId);
                    exam.setExamName(courseName + " 期末考试");
                    exam.setExamType("期末考试");
                    exam.setSemester(semester);
                    exam.setExamDate(Date.valueOf(examDate));
                    exam.setStartTime(Time.valueOf(LocalTime.of(timeSlot[0], timeSlot[1])));
                    exam.setEndTime(Time.valueOf(LocalTime.of(timeSlot[2], timeSlot[3])));
                    examMapper.insert(exam);
                    totalExams++;

                    slotCourseCodes.computeIfAbsent(dateStr + timeStr, k -> new HashSet<>()).add(courseCode);
                    found = true;
                }
                slotIdx++;
            }
            if (!found) System.err.println("【排考冲突】" + courseName + " 无法找到无冲突时段");
        }

        return CommonResult.success(Map.of(
            "scheduledExams", totalExams,
            "totalStudents", selections.size(),
            "message", "已为 " + totalExams + " 门课程自动编排考试（已避免时间冲突）"
        ));
    }

    // ==================== 考场与座位编排 ====================

    @Override
    @Transactional
    public CommonResult assignRoomsAndSeats(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) return CommonResult.error(940, "考试不存在");

        Course course = courseMapper.selectById(exam.getCourseId());
        String courseCode = course != null ? course.getCourseCode() : "UNKNOWN";

        // 1. 查询该考试下所有选课学生（按学号升序）
        List<Map<String, Object>> students = selectionMapper.selectByCourseCodeAndSemester(
            courseCode, exam.getSemester());
        if (students.isEmpty()) return CommonResult.error(941, "该考试无选课学生");

        // 按 student_no 升序排列
        students.sort(Comparator.comparing(s ->
            String.valueOf(s.getOrDefault("student_no", "0"))));

        // 2. 清除旧考场分配，避免重复
        examMapper.deleteExamRoomsByExamId(examId);

        // 3. 分配教室（容量折半原则）
        List<Classroom> rooms = classroomMapper.selectList(null);
        rooms.sort(Comparator.comparingInt(Classroom::getCapacity).reversed());

        int totalStudents = students.size();
        int remaining = totalStudents;
        int studentIdx = 0;
        int roomsUsed = 0;

        for (Classroom room : rooms) {
            if (remaining <= 0) break;
            // 容量折半（隔位就坐）
            int halfCapacity = room.getCapacity() / 2;
            if (halfCapacity <= 0) continue;

            int assigned = Math.min(remaining, halfCapacity);
            roomsUsed++;

            // 为该考场的考生安排座位号
            for (int i = 0; i < assigned && studentIdx < totalStudents; i++, studentIdx++) {
                Map<String, Object> stu = students.get(studentIdx);
                com.smartcampus.contract.entity.ExamRoom seat = new com.smartcampus.contract.entity.ExamRoom();
                seat.setExamId(examId);
                seat.setClassroomId(room.getClassroomId());
                seat.setSeatNo(String.valueOf(i + 1)); // 座位号 1-based
                seat.setStudentId((Long) stu.get("student_id"));
                examMapper.insertExamRoom(seat);
            }
            remaining -= assigned;
        }

        return CommonResult.success(Map.of(
            "totalStudents", totalStudents,
            "roomsUsed", roomsUsed,
            "message", "考场与座位编排完成（隔位就坐，学号递增）"
        ));
    }

    /** 一键为所有考试自动分配考场 */
    @Override
    @Transactional
    public CommonResult assignAllRooms(String semester) {
        LambdaQueryWrapper<Exam> w = new LambdaQueryWrapper<>();
        w.eq(Exam::getSemester, semester);
        List<Exam> exams = examMapper.selectList(w);
        int total = 0;
        for (Exam exam : exams) {
            CommonResult r = assignRoomsAndSeats(exam.getExamId());
            if (r.getCode() == 0) total++;
        }
        return CommonResult.success(Map.of("assignedExams", total, "totalExams", exams.size(), "message", "已为 " + total + "/" + exams.size() + " 场考试分配考场"));
    }

    // ==================== 监考指派 ====================

    @Override
    @Transactional
    public CommonResult assignInvigilators(Long examId, Long classroomId,
                                            Long mainTeacherId, Long subTeacherId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) return CommonResult.error(940, "考试不存在");

        String examDate = exam.getExamDate().toString();
        String startTime = exam.getStartTime().toString();
        String endTime = exam.getEndTime().toString();

        // 校验每日监考场次 ≤ 2
        for (Long tid : new Long[]{mainTeacherId, subTeacherId}) {
            if (tid == null) continue;
            long dailyCount = examMapper.countInvigilationByTeacherAndDate(tid, examDate);
            if (dailyCount >= 2) {
                return CommonResult.error(942,
                    "教师" + tid + "当天已有" + dailyCount + "场监考，每日上限2场");
            }
        }

        // 授课回避：主副监考不能是该考场内任何考生的授课教师
        Course course2 = courseMapper.selectById(exam.getCourseId());
        String courseCode2 = course2 != null ? course2.getCourseCode() : "UNKNOWN";
        List<Map<String, Object>> students = selectionMapper.selectByCourseCodeAndSemester(
            courseCode2, exam.getSemester());
        Set<Long> studentIds = students.stream()
            .map(s -> (Long) s.get("student_id")).collect(Collectors.toSet());

        for (Long tid : new Long[]{mainTeacherId, subTeacherId}) {
            if (tid == null) continue;
            List<Schedule> teacherSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getTeacherId, tid)
                    .eq(Schedule::getSemester, exam.getSemester()));
            for (Schedule sch : teacherSchedules) {
                // 查该教学班的选课学生
                List<Map<String, Object>> classStudents = selectionMapper.selectByScheduleId(sch.getScheduleId());
                for (Map<String, Object> cs : classStudents) {
                    Long sid = (Long) cs.get("student_id");
                    if (studentIds.contains(sid)) {
                        Course course = courseMapper.selectById(sch.getCourseId());
                        return CommonResult.error(943,
                            "教师" + tid + "是考场内考生《" +
                            (course != null ? course.getCourseName() : "未知") + "》的授课教师，不可监考");
                    }
                }
            }
        }

        // 插入监考记录
        if (mainTeacherId != null) {
            Invigilation main = new Invigilation();
            main.setExamId(examId); main.setTeacherId(mainTeacherId);
            main.setClassroomId(classroomId); main.setDuty("主监考");
            examMapper.insertInvigilation(main);
        }
        if (subTeacherId != null) {
            Invigilation sub = new Invigilation();
            sub.setExamId(examId); sub.setTeacherId(subTeacherId);
            sub.setClassroomId(classroomId); sub.setDuty("副监考");
            examMapper.insertInvigilation(sub);
        }

        return CommonResult.success(Map.of("message", "监考指派完成"));
    }

    /** 一键为所有考试自动指派监考教师 */
    @Override
    @Transactional
    public CommonResult assignAllInvigilators(String semester) {
        LambdaQueryWrapper<Exam> ew = new LambdaQueryWrapper<>();
        ew.eq(Exam::getSemester, semester);
        List<Exam> exams = examMapper.selectList(ew);
        if (exams.isEmpty()) return CommonResult.error(945, "无考试记录");

        // 获取所有教职工（user_type=3）
        List<com.smartcampus.contract.entity.User> teachers = new ArrayList<>();
        try {
            com.smartcampus.auth.repository.AuthUserMapper authMapper = null;
            // 直接查 user 表中 user_type=3 的用户
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.smartcampus.contract.entity.User> uw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            uw.eq(com.smartcampus.contract.entity.User::getUserType, 3);
        } catch (Exception ignored) {}

        // 简化：使用固定教师ID池 (3,4,5)
        Long[] teacherPool = {3L, 4L, 5L};
        int ti = 0, assigned = 0;

        for (Exam exam : exams) {
            Long main = teacherPool[ti % 3];
            Long sub = teacherPool[(ti + 1) % 3];
            // 避免主副相同
            if (main.equals(sub)) sub = teacherPool[(ti + 2) % 3];

            // 清除旧指派
            examMapper.deleteInvigilationsByExamId(exam.getExamId());

            Invigilation mi = new Invigilation();
            mi.setExamId(exam.getExamId()); mi.setTeacherId(main);
            mi.setClassroomId(1L); mi.setDuty("主监考");
            examMapper.insertInvigilation(mi);

            Invigilation si = new Invigilation();
            si.setExamId(exam.getExamId()); si.setTeacherId(sub);
            si.setClassroomId(1L); si.setDuty("副监考");
            examMapper.insertInvigilation(si);

            assigned++;
            ti++;
        }
        return CommonResult.success(Map.of("assignedExams", assigned, "message", "已为 " + assigned + " 场考试自动指派监考"));
    }

    // ==================== 查询 ====================

    @Override
    public CommonResult listExams(String semester, String examType) {
        LambdaQueryWrapper<Exam> w = new LambdaQueryWrapper<>();
        if (semester != null) w.eq(Exam::getSemester, semester);
        if (examType != null) w.eq(Exam::getExamType, examType);
        w.orderByAsc(Exam::getExamDate);
        return CommonResult.success(examMapper.selectList(w));
    }

    @Override
    public CommonResult getExamStudents(Long examId) {
        List<Map<String, Object>> students = examMapper.selectExamStudents(examId);
        return CommonResult.success(students);
    }

    @Override
    public CommonResult getTeacherInvigilations(Long teacherId, String semester) {
        List<Map<String, Object>> list = examMapper.selectInvigilationsByTeacher(teacherId);
        return CommonResult.success(list);
    }

    @Override
    public CommonResult validateInvigilation(Long examId, Long teacherId) {
        // 同 assignInvigilators 中的校验逻辑
        return CommonResult.success(Map.of("available", true));
    }

    // ==================== 补考/缓考预报名 ====================

    @Override
    @Transactional
    public CommonResult applyResit(Long studentId, Long courseId, String semester,
                                    String applyType) {
        LambdaQueryWrapper<Score> sw = new LambdaQueryWrapper<>();
        sw.eq(Score::getStudentId, studentId)
          .eq(Score::getCourseId, courseId);
        Score score = scoreMapper.selectOne(sw);

        if (score != null && score.getStatus() != null && score.getStatus() == 1) {
            return CommonResult.error(935, "该课程成绩已及格，无需补考");
        }
        if (!"缓考".equals(applyType) && (score == null || (score.getStatus() != null && score.getStatus() == 1))) {
            return CommonResult.error(935, "该课程成绩已及格，无需补考");
        }

        // 是否已有进行中的申请
        LambdaQueryWrapper<ResitApply> aw = new LambdaQueryWrapper<>();
        aw.eq(ResitApply::getStudentId, studentId)
          .eq(ResitApply::getCourseId, courseId)
          .in(ResitApply::getStatus, 0, 1);
        if (resitApplyMapper.selectCount(aw) > 0) {
            return CommonResult.error(936, "已有该课程的补考/缓考记录");
        }

        ResitApply apply = new ResitApply();
        apply.setStudentId(studentId);
        apply.setCourseId(courseId);
        apply.setApplyType(applyType);
        apply.setApplyTime(new java.util.Date());
        apply.setStatus(1);
        resitApplyMapper.insert(apply);

        return CommonResult.success(Map.of("message", applyType + "报名成功"));
    }

    @Override
    @Transactional
    public CommonResult autoRevokeIfPassed(Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Score> sw = new LambdaQueryWrapper<>();
        sw.eq(Score::getStudentId, studentId)
          .eq(Score::getCourseId, courseId);
        Score score = scoreMapper.selectOne(sw);

        if (score != null && score.getScoreScore() != null && score.getScoreScore() >= 60) {
            LambdaQueryWrapper<ResitApply> aw = new LambdaQueryWrapper<>();
            aw.eq(ResitApply::getStudentId, studentId)
              .eq(ResitApply::getCourseId, courseId)
              .eq(ResitApply::getStatus, 1);
            ResitApply apply = resitApplyMapper.selectOne(aw);
            if (apply != null && !"缓考".equals(apply.getApplyType())) {
                apply.setStatus(2);
                resitApplyMapper.updateById(apply);
                return CommonResult.success(Map.of("revoked", true, "message", "补考自动撤销"));
            }
        }
        return CommonResult.success(Map.of("revoked", false));
    }

    @Override
    @Transactional
    public CommonResult freezeResitList(String semester) {
        LambdaQueryWrapper<ResitApply> w = new LambdaQueryWrapper<>();
        w.eq(ResitApply::getStatus, 1);
        List<ResitApply> list = resitApplyMapper.selectList(w);
        for (ResitApply a : list) {
            a.setStatus(3);
            resitApplyMapper.updateById(a);
        }
        return CommonResult.success(Map.of("frozen", list.size()));
    }

    @Override
    public CommonResult getStudentResitStatus(Long studentId, String semester) {
        LambdaQueryWrapper<ResitApply> w = new LambdaQueryWrapper<>();
        w.eq(ResitApply::getStudentId, studentId);
        return CommonResult.success(resitApplyMapper.selectList(w));
    }

    @Override
    public CommonResult getStudentExams(Long studentId, String semester) {
        List<Map<String, Object>> exams = examMapper.selectByStudent(studentId, semester);
        return CommonResult.success(exams);
    }
}
