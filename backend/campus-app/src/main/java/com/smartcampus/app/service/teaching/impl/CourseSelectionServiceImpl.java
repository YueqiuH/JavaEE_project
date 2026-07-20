package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.CourseCapacityMapper;
import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.app.dao.teaching.CourseSelectionMapper;
import com.smartcampus.app.dao.teaching.ScheduleMapper;
import com.smartcampus.app.dao.teaching.ScoreMapper;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.CourseCapacity;
import com.smartcampus.contract.entity.Course;
import com.smartcampus.contract.entity.CourseSelection;
import com.smartcampus.contract.entity.Schedule;
import com.smartcampus.contract.entity.Score;
import com.smartcampus.app.service.teaching.ICourseSelectionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CourseSelectionServiceImpl
        extends ServiceImpl<CourseSelectionMapper, CourseSelection>
        implements ICourseSelectionService {

    @Autowired
    CourseSelectionMapper selectionMapper;

    @Autowired
    CourseCapacityMapper capacityMapper;

    @Autowired
    ScheduleMapper scheduleMapper;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    ScoreMapper scoreMapper;

    /**
     * 学生选课 —— 三重校验 + 事务保护
     *
     * 校验步骤：
     * 1. 容量校验：已选人数 < 最大容量
     * 2. 重复校验：同一学生同一课程同一学期不可重复选
     * 3. 时间冲突校验：新课与已选课表不重叠
     */
    @Override
    @Transactional
    public CommonResult selectCourse(Long studentId, Long courseId, String semester) {

        System.out.println("选课请求: 学生=" + studentId + " 课程=" + courseId + " 学期=" + semester);

        // ===== 第1步：容量校验（漏洞9: 下沉到教学班级别） =====
        // 获取课程信息
        Course courseEntity = courseMapper.selectById(courseId);
        if (courseEntity == null) {
            return CommonResult.error(910, "课程不存在");
        }

        // 漏洞9: 按 schedule_id 查容量（教学班级别），否则按 course_id 查（兼容旧数据）
        LambdaQueryWrapper<CourseCapacity> capWrapper = new LambdaQueryWrapper<>();
        capWrapper.eq(CourseCapacity::getCourseId, courseId)
                   .eq(CourseCapacity::getSemester, semester);
        // 优先匹配 schedule_id 不为空的记录
        capWrapper.isNotNull(CourseCapacity::getScheduleId);
        List<CourseCapacity> capacities = capacityMapper.selectList(capWrapper);

        // 如果教学班容量为空，回退到课程级别查询
        if (capacities.isEmpty()) {
            capWrapper = new LambdaQueryWrapper<>();
            capWrapper.eq(CourseCapacity::getCourseId, courseId)
                       .eq(CourseCapacity::getSemester, semester);
            capacities = capacityMapper.selectList(capWrapper);
        }

        if (capacities.isEmpty()) {
            return CommonResult.error(910, "该课程本学期未开课，请选择其他课程");
        }

        // 选第一个有容量的班
        CourseCapacity capacity = null;
        for (CourseCapacity c : capacities) {
            if (c.getCurrentCount() < c.getMaxCapacity()) {
                capacity = c; break;
            }
        }
        if (capacity == null) {
            return CommonResult.error(911, "该课程所有教学班均已选满，请选择其他课程");
        }

        // ===== 第2步：重复选课校验 + 漏洞8: 同课防重选锁 =====
        LambdaQueryWrapper<CourseSelection> selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(CourseSelection::getStudentId, studentId)
                   .eq(CourseSelection::getCourseId, courseId)
                   .eq(CourseSelection::getSemester, semester)
                   .eq(CourseSelection::getStatus, 1);
        if (selectionMapper.selectCount(selWrapper) > 0) {
            return CommonResult.error(912, "你已经选过该课程，请勿重复选择");
        }

        // 漏洞8: 同课防重选锁——同一个 course_code 下选了多个教学班
        String targetCourseCode = courseEntity.getCourseCode();
        if (targetCourseCode != null && !targetCourseCode.isEmpty()) {
            List<Map<String, Object>> mySelections =
                selectionMapper.selectByStudentWithCourse(studentId, semester);
            for (Map<String, Object> sel : mySelections) {
                // 通过已选记录的 course_id 查出 course_code
                Long selCourseId = (Long) sel.get("course_id");
                if (selCourseId != null && !selCourseId.equals(courseId)) {
                    Course selCourse = courseMapper.selectById(selCourseId);
                    if (selCourse != null && targetCourseCode.equals(selCourse.getCourseCode())) {
                        return CommonResult.error(919,
                            "您已选了课程代码为《" + targetCourseCode + "》的另一个教学班（"
                            + selCourse.getCourseName() + "），同一课程代码下只能选一个教学班。");
                    }
                }
            }
        }

        // ===== F3: 先修课校验（courseEntity 已在第1步获取） =====
        if (courseEntity.getPrerequisiteId() != null) {
            LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(Score::getStudentId, studentId)
                       .eq(Score::getCourseId, courseEntity.getPrerequisiteId())
                       .eq(Score::getStatus, 1); // 1=已通过
            Long passCount = scoreMapper.selectCount(scoreWrapper);
            if (passCount == null || passCount == 0) {
                Course prereq = courseMapper.selectById(courseEntity.getPrerequisiteId());
                String prereqName = prereq != null ? prereq.getCourseName() : "未知课程";
                return CommonResult.error(918,
                    "您尚未取得先修课程《" + prereqName + "》的学分，无法修读本课程。");
            }
        }

        // ===== 第3步：时间冲突校验 =====
        // 查询新课的排课时间
        LambdaQueryWrapper<Schedule> newSchWrapper = new LambdaQueryWrapper<>();
        newSchWrapper.eq(Schedule::getCourseId, courseId)
                      .eq(Schedule::getSemester, semester);
        List<Schedule> newSchedules = scheduleMapper.selectList(newSchWrapper);

        // 查询学生已有课表
        List<Schedule> existingSchedules = scheduleMapper.selectByStudent(studentId, semester);

        // 逐条比对是否存在时间重叠
        if (newSchedules != null && existingSchedules != null) {
            for (Schedule existing : existingSchedules) {
                for (Schedule newSch : newSchedules) {
                    if (isTimeOverlap(existing, newSch)) {
                        Course conflictCourse = courseMapper.selectById(existing.getCourseId());
                        String conflictName = conflictCourse != null ? conflictCourse.getCourseName() : "未知课程";
                        return CommonResult.error(913,
                            "与已选课程《" + conflictName + "》时间冲突！（星期" + existing.getWeekDay()
                            + " 第" + existing.getStartPeriod() + "-" + existing.getEndPeriod() + "节）");
                    }
                }
            }
        }

        // ===== 第4步：执行选课 =====
        // 检查是否存在退选记录（status=2），有则复用 update，无则 insert
        LambdaQueryWrapper<CourseSelection> droppedWrapper = new LambdaQueryWrapper<>();
        droppedWrapper.eq(CourseSelection::getStudentId, studentId)
                      .eq(CourseSelection::getCourseId, courseId)
                      .eq(CourseSelection::getSemester, semester)
                      .eq(CourseSelection::getStatus, 2);
        CourseSelection existDropped = selectionMapper.selectOne(droppedWrapper);

        if (existDropped != null) {
            // 复用已退选记录，更新状态为已选
            existDropped.setStatus(1);
            existDropped.setSelectTime(new Date());
            if (newSchedules != null && !newSchedules.isEmpty()) {
                existDropped.setScheduleId(newSchedules.get(0).getScheduleId());
            }
            selectionMapper.updateById(existDropped);
        } else {
            // 全新选课
            CourseSelection selection = new CourseSelection();
            selection.setStudentId(studentId);
            selection.setCourseId(courseId);
            selection.setSemester(semester);
            selection.setStatus(1);
            selection.setSelectTime(new Date());
            if (newSchedules != null && !newSchedules.isEmpty()) {
                selection.setScheduleId(newSchedules.get(0).getScheduleId());
            }
            selectionMapper.insert(selection);
        }

        // 漏洞14: 原子更新容量——绝对禁止 read-check-write 的脏读模式
        int affected = capacityMapper.incrementCount(capacity.getCapacityId());
        if (affected == 0) {
            // 行级锁判定：在高并发下，此时容量已被其他事务抢先填满
            throw new RuntimeException("选课失败：该教学班在您确认前刚刚满员，请刷新后重试");
        }

        System.out.println("选课成功: 学生=" + studentId + " 课程=" + courseId);
        return CommonResult.success();
    }

    /**
     * 学生退选
     */
    @Override
    @Transactional
    public CommonResult dropCourse(Long studentId, Long courseId, String semester) {

        // 查找选课记录
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
               .eq(CourseSelection::getCourseId, courseId)
               .eq(CourseSelection::getSemester, semester)
               .eq(CourseSelection::getStatus, 1);
        CourseSelection selection = selectionMapper.selectOne(wrapper);

        if (selection == null) {
            return CommonResult.error(914, "未找到该选课记录，无法退选");
        }

        // 更新为退选状态
        selection.setStatus(2);
        selectionMapper.updateById(selection);

        // 漏洞14: 原子减少容量——避免并发脏读
        LambdaQueryWrapper<CourseCapacity> capWrapper = new LambdaQueryWrapper<>();
        capWrapper.eq(CourseCapacity::getCourseId, courseId)
                   .eq(CourseCapacity::getSemester, semester);
        CourseCapacity capacity = capacityMapper.selectOne(capWrapper);
        if (capacity != null) {
            capacityMapper.decrementCount(capacity.getCapacityId());
        }

        System.out.println("退选成功: 学生=" + studentId + " 课程=" + courseId);
        return CommonResult.success();
    }

    /**
     * 教务查看某课程的选课学生名单
     */
    @Override
    public CommonResult getStudentListByCourse(Long courseId, String semester) {
        List<Map<String, Object>> studentList =
            selectionMapper.selectStudentListByCourse(courseId, semester);
        return CommonResult.success(studentList);
    }

    /**
     * 学生查看我的选课列表
     */
    @Override
    public CommonResult getMySelection(Long studentId, String semester) {
        List<Map<String, Object>> myCourses =
            selectionMapper.selectByStudentWithCourse(studentId, semester);
        return CommonResult.success(myCourses);
    }

    /**
     * 教务调整课程容量
     */
    @Override
    public CommonResult updateCapacity(Long capacityId, Integer maxCapacity, Integer minCapacity) {
        CourseCapacity capacity = capacityMapper.selectById(capacityId);
        if (capacity == null) {
            return CommonResult.error(915, "容量配置不存在");
        }
        if (maxCapacity != null) capacity.setMaxCapacity(maxCapacity);
        if (minCapacity != null) capacity.setMinCapacity(minCapacity);
        capacityMapper.updateById(capacity);

        System.out.println("容量更新: capacityId=" + capacityId
            + " max=" + maxCapacity + " min=" + minCapacity);
        return CommonResult.success();
    }

    /**
     * 时间冲突判断：两个排课时间段是否重叠
     * 重叠条件 = 星期相同 + 周次有交集 + 节次有交集
     */
    private boolean isTimeOverlap(Schedule a, Schedule b) {
        if (!a.getWeekDay().equals(b.getWeekDay())) return false;
        // 周次有交集
        boolean weekOverlap = a.getStartWeek() <= b.getEndWeek()
                           && a.getEndWeek() >= b.getStartWeek();
        // 节次有交集
        boolean periodOverlap = a.getStartPeriod() < b.getEndPeriod()
                             && a.getEndPeriod() > b.getStartPeriod();
        return weekOverlap && periodOverlap;
    }
}
