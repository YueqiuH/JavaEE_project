# 成员 A 实现指南：教务核心、学业管理与 AI 智能学习助手

> **Controller 包**：`com.smartcampus.app.controller.teaching`
> **Service 包**：`com.smartcampus.app.service.teaching`
> **Mapper 包**：`com.smartcampus.app.dao.teaching`
> **Entity 包**：`com.smartcampus.contract.entity`
> **涉及表**：classroom, schedule, schedule_change, course_selection, course_capacity, score, exam, exam_room, invigilation, resit_apply, graduation_topic, graduation_selection, graduation_report, study_material, ai_study_record

---

## 开发规范速查

| 项目 | 规范 |
|------|------|
| 实体类 | `@Data` + `@ToString` + `@TableName` + `@TableId(type = IdType.AUTO)` + `implements Serializable` |
| Mapper | `extends BaseMapper<Entity>`，接口定义在 `dao` 包，复杂 SQL 写在 XML 中 |
| Service 接口 | `extends IService<Entity>`，命名 `I{Name}Service` |
| Service 实现 | `extends ServiceImpl<Mapper, Entity> implements I{Name}Service`，`@Service` 注解 |
| Controller | `@RestController` + `@RequestMapping("/teaching/xxx")`，`@Tag` + `@Operation` 中文注释 |
| 依赖注入 | `@Autowired` 字段注入 |
| 响应封装 | `CommonResult<T>` 统一返回，`CommonResult.success(data)` / `CommonResult.error(ErrorCode)` |
| 分页查询 | `Page<Entity>(pageNo, pageSize)` + `LambdaQueryWrapper` + `this.page(page, wrapper)` |
| 日志输出 | `System.out.println()` 用于调试，关键节点打印 |
| 前端组件 | `<script setup>` 组合式 API，Element Plus 组件库 |
| API 调用 | `.then(res => { if (res && res != -1) {...} })` 模式 |

---

## A1：排课与课表管理系统

### 1.1 业务闭环详解

```
┌──────────────────────────────────────────────────────────────────┐
│                        排课与课表管理                              │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐   ①导入课程/教室    ┌────────────┐                  │
│  │ 教务人员 │ ─────────────────→ │  排课系统   │                  │
│  │         │   ②新增排课(手动)   │            │                  │
│  │         │   ③删除排课        │  冲突校验   │                  │
│  │         │   ④审批调课申请     │  自动检测   │                  │
│  └─────────┘                    └─────┬──────┘                  │
│                                       │                          │
│              ┌────────────────────────┼──────────────────┐       │
│              │ ⑤查询日历课表          │  ⑤查询日历课表    │       │
│              ↓                        ↓                  │       │
│        ┌─────────┐             ┌─────────┐              │       │
│        │  学生   │             │  教师   │              │       │
│        │ 我的课表 │             │ 我的课表 │              │       │
│        └─────────┘             └────┬────┘              │       │
│                                     │                    │       │
│                          ⑥提交调课申请                    │       │
│                          (改时间/改教室)                  │       │
│                                     ↓                    │       │
│                              ┌──────────┐               │       │
│                              │ 调课审批  │               │       │
│                              │ 通过/拒绝 │               │       │
│                              └──────────┘               │       │
└──────────────────────────────────────────────────────────────────┘
```

### 1.2 完整后端实现

#### ScheduleMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.smartcampus.app.dao.teaching.ScheduleMapper">

    <resultMap id="ScheduleVoMap" type="com.smartcampus.contract.entity.Schedule">
        <id column="schedule_id" property="scheduleId"/>
        <result column="course_id" property="courseId"/>
        <result column="classroom_id" property="classroomId"/>
        <result column="teacher_id" property="teacherId"/>
        <result column="semester" property="semester"/>
        <result column="week_day" property="weekDay"/>
        <result column="start_period" property="startPeriod"/>
        <result column="end_period" property="endPeriod"/>
        <result column="start_week" property="startWeek"/>
        <result column="end_week" property="endWeek"/>
        <result column="schedule_type" property="scheduleType"/>
    </resultMap>

    <!-- 按教师查询课表（含课程名、教室名） -->
    <select id="selectByTeacher" resultMap="ScheduleVoMap">
        SELECT s.*, c.course_name, cr.classroom_name
        FROM schedule s
        LEFT JOIN course c ON s.course_id = c.course_id
        LEFT JOIN classroom cr ON s.classroom_id = cr.classroom_id
        WHERE s.teacher_id = #{teacherId}
          AND s.semester = #{semester}
        ORDER BY s.week_day, s.start_period
    </select>

    <!-- 按学生查询课表（关联选课表） -->
    <select id="selectByStudent" resultMap="ScheduleVoMap">
        SELECT s.*, c.course_name, cr.classroom_name
        FROM schedule s
        INNER JOIN course_selection cs ON s.course_id = cs.course_id
            AND cs.student_id = #{studentId} AND cs.semester = #{semester} AND cs.status = 1
        LEFT JOIN course c ON s.course_id = c.course_id
        LEFT JOIN classroom cr ON s.classroom_id = cr.classroom_id
        WHERE s.semester = #{semester}
        ORDER BY s.week_day, s.start_period
    </select>
</mapper>
```

#### ScheduleServiceImpl —— 完整业务逻辑

```java
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule>
        implements IScheduleService {

    @Autowired ScheduleMapper scheduleMapper;
    @Autowired ClassroomMapper classroomMapper;
    @Autowired ScheduleChangeMapper changeMapper;
    @Autowired CourseMapper courseMapper;

    /**
     * 教务新增排课 —— 含完整的冲突校验
     */
    @Override
    public CommonResult addSchedule(Schedule schedule) {
        // 1. 校验教室是否存在且可用
        Classroom room = classroomMapper.selectById(schedule.getClassroomId());
        if (room == null || room.getStatus() != 1) {
            return CommonResult.error(ErrorCode(901, "教室不存在或维护中"));
        }
        // 2. 校验课程是否存在
        if (courseMapper.selectById(schedule.getCourseId()) == null) {
            return CommonResult.error(ErrorCode(902, "课程不存在"));
        }
        // 3. 节次合法性校验
        if (schedule.getStartPeriod() >= schedule.getEndPeriod()) {
            return CommonResult.error(ErrorCode(903, "开始节次必须小于结束节次"));
        }
        if (schedule.getStartPeriod() < 1 || schedule.getEndPeriod() > 12) {
            return CommonResult.error(ErrorCode(904, "节次范围必须在 1-12 之间"));
        }
        // 4. 教室时间冲突校验（同一教室同一天同时段不能有两条排课）
        if (hasClassroomConflict(schedule)) {
            return CommonResult.error(ErrorCode(905, "该教室在此时段已被占用"));
        }
        // 5. 教师时间冲突校验（同一教师同一天同时段不能有两门课）
        if (hasTeacherConflict(schedule)) {
            return CommonResult.error(ErrorCode(906, "该教师在此时段已有其他课程"));
        }
        // 6. 班级/年级冲突校验（可选——同一班级不可同时上两门课）
        // ...

        this.baseMapper.insert(schedule);
        return CommonResult.success();
    }

    /**
     * 教室时间冲突检测（核心算法）
     *
     * 冲突条件：同一学期 + 同一教室 + 星期相同 + 周次有交集 + 节次有交集
     */
    private boolean hasClassroomConflict(Schedule schedule) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getClassroomId, schedule.getClassroomId())
               .eq(Schedule::getSemester, schedule.getSemester())
               .eq(Schedule::getWeekDay, schedule.getWeekDay())
               // 周次有交集：existing.startWeek <= new.endWeek AND existing.endWeek >= new.startWeek
               .le(Schedule::getStartWeek, schedule.getEndWeek())
               .ge(Schedule::getEndWeek, schedule.getStartWeek())
               // 节次有交集：existing.startPeriod < new.endPeriod AND existing.endPeriod > new.startPeriod
               .lt(Schedule::getStartPeriod, schedule.getEndPeriod())
               .gt(Schedule::getEndPeriod, schedule.getStartPeriod());
        // 如果是更新操作，排除自身
        if (schedule.getScheduleId() != null) {
            wrapper.ne(Schedule::getScheduleId, schedule.getScheduleId());
        }
        return this.count(wrapper) > 0;
    }

    /** 教师时间冲突检测 —— 逻辑同上，按 teacher_id 检查 */
    private boolean hasTeacherConflict(Schedule schedule) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getTeacherId, schedule.getTeacherId())
               .eq(Schedule::getSemester, schedule.getSemester())
               .eq(Schedule::getWeekDay, schedule.getWeekDay())
               .le(Schedule::getStartWeek, schedule.getEndWeek())
               .ge(Schedule::getEndWeek, schedule.getStartWeek())
               .lt(Schedule::getStartPeriod, schedule.getEndPeriod())
               .gt(Schedule::getEndPeriod, schedule.getStartPeriod());
        if (schedule.getScheduleId() != null) {
            wrapper.ne(Schedule::getScheduleId, schedule.getScheduleId());
        }
        return this.count(wrapper) > 0;
    }

    /**
     * 学生课表查询 —— 按星期几分组，返回 JSONObject
     *
     * 返回格式：
     * {
     *   "1": [{ courseName, classroomName, teacherName, startPeriod, endPeriod, startWeek, endWeek }, ...],
     *   "2": [...], ...
     *   "7": [...]
     * }
     */
    @Override
    public JSONObject getStudentSchedule(Long studentId, String semester) {
        List<Schedule> list = scheduleMapper.selectByStudent(studentId, semester);
        JSONObject result = new JSONObject();
        // 初始化 7 天
        for (int day = 1; day <= 7; day++) {
            result.put(String.valueOf(day), new JSONArray());
        }
        // 按 weekDay 分组
        for (Schedule s : list) {
            JSONArray dayArray = result.getJSONArray(String.valueOf(s.getWeekDay()));
            JSONObject item = new JSONObject();
            item.put("scheduleId", s.getScheduleId());
            item.put("courseId", s.getCourseId());
            item.put("classroomId", s.getClassroomId());
            item.put("startPeriod", s.getStartPeriod());
            item.put("endPeriod", s.getEndPeriod());
            item.put("startWeek", s.getStartWeek());
            item.put("endWeek", s.getEndWeek());
            item.put("scheduleType", s.getScheduleType());
            dayArray.add(item);
        }
        return result;
    }

    /**
     * 调课申请 —— 教师提交
     */
    @Override
    public CommonResult applyScheduleChange(ScheduleChange change) {
        // 校验原排课是否存在
        Schedule origin = this.getById(change.getScheduleId());
        if (origin == null) {
            return CommonResult.error(ErrorCode(907, "原排课记录不存在"));
        }
        change.setStatus(0); // 待审批
        change.setCreateTime(new Date());
        changeMapper.insert(change);
        return CommonResult.success();
    }

    /**
     * 调课审批 —— 教务操作
     */
    @Override
    public CommonResult approveScheduleChange(Long changeId, Integer status, String remark) {
        ScheduleChange change = changeMapper.selectById(changeId);
        if (change == null || change.getStatus() != 0) {
            return CommonResult.error(ErrorCode(908, "申请不存在或已处理"));
        }

        change.setStatus(status);         // 1=通过, 2=拒绝
        change.setApproveRemark(remark);
        changeMapper.updateById(change);

        // 如果审批通过，更新原排课记录
        if (status == 1) {
            Schedule schedule = this.getById(change.getScheduleId());
            if (change.getNewWeekDay() != null) schedule.setWeekDay(change.getNewWeekDay());
            if (change.getNewStartPeriod() != null) schedule.setStartPeriod(change.getNewStartPeriod());
            if (change.getNewEndPeriod() != null) schedule.setEndPeriod(change.getNewEndPeriod());
            if (change.getNewClassroomId() != null) schedule.setClassroomId(change.getNewClassroomId());
            schedule.setScheduleType("调课");
            this.updateById(schedule);
        }
        return CommonResult.success();
    }
}
```

### 1.3 前端实现

**路由**：`/home/course-schedule`

**课表日历完整模板逻辑**：

```vue
<template>
  <div class="schedule-page">
    <!-- 顶部切换栏 -->
    <div class="schedule-toolbar">
      <el-select v-model="currentSemester" @change="loadSchedule">
        <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
      </el-select>
      <el-radio-group v-model="viewMode">
        <el-radio-button value="week">周视图</el-radio-button>
        <el-radio-button value="list">列表视图</el-radio-button>
      </el-radio-group>
      <span class="current-week">当前第 {{ currentWeek }} 周</span>
      <el-button-group>
        <el-button @click="prevWeek">上一周</el-button>
        <el-button @click="nextWeek">下一周</el-button>
      </el-button-group>
    </div>

    <!-- 周视图：7列 x 12行 课表网格 -->
    <div v-if="viewMode === 'week'" class="week-grid">
      <div class="grid-header">
        <div class="period-col">节次</div>
        <div v-for="d in 7" :key="d" class="day-col">
          星期{{ ['一','二','三','四','五','六','日'][d-1] }}
        </div>
      </div>
      <div v-for="p in 12" :key="p" class="grid-row">
        <div class="period-label">第{{ p }}节</div>
        <div v-for="d in 7" :key="d" class="grid-cell"
             :class="{ 'has-course': getCourseAt(d, p) }"
             @click="handleCellClick(d, p)">
          <div v-if="getCourseAt(d, p)" class="course-block"
               :style="{ height: getCourseHeight(d, p) + 'px' }">
            <div class="course-name">{{ getCourseAt(d, p).courseName }}</div>
            <div class="course-room">{{ getCourseAt(d, p).classroomName }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getStudentSchedule, getTeacherSchedule } from '@/api/getData'

const currentSemester = ref('2025-2026-1')
const currentWeek = ref(1)
const viewMode = ref('week')
const scheduleData = ref({})  // { 1: [...], 2: [...], ... 7: [...] }

const loadSchedule = () => {
    const userId = JSON.parse(getStorage('userInfo')).user.userId
    getStudentSchedule(userId, currentSemester.value).then(res => {
        if (res && res != -1) scheduleData.value = res.data
    })
}

// 获取指定星期几、指定节次的课程
const getCourseAt = (weekDay, period) => {
    const courses = scheduleData.value[String(weekDay)] || []
    return courses.find(c =>
        period >= c.startPeriod && period <= c.endPeriod
        && currentWeek.value >= c.startWeek && currentWeek.value <= c.endWeek
    ) || null
}

// 计算课程卡片高度（跨节次）
const getCourseHeight = (weekDay, period) => {
    const course = getCourseAt(weekDay, period)
    if (!course || period !== course.startPeriod) return 0
    return (course.endPeriod - course.startPeriod + 1) * 60
}
</script>
```

**子页面拆分**（建议将教务管理端和学生查询端分开）：
| 文件 | 用途 |
|------|------|
| `views/teaching/CourseSchedule.vue` | 学生/教师课表查询（共用，切换身份） |
| `views/teaching/ScheduleManage.vue` | 教务排课管理（el-table + 新增/编辑弹窗 + 冲突提示） |
| `views/teaching/ScheduleChange.vue` | 调课申请列表 + 审批操作（教师提交 / 教务审批两套视图） |

---

## A2：选课与容量控制系统

### 2.1 业务闭环详解

```
┌──────────────────────────────────────────────────────────────┐
│                      选课与容量控制                            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  【选课轮次控制】                                              │
│  教务设定: 选课开始时间 / 选课结束时间 / 是否开放退选            │
│                                                              │
│  【学生选课流程】                                              │
│  ① 浏览可选课程列表(按学期/院系/类型筛选)                       │
│  ② 查看课程详情(教师/时间/教室/已选人数/容量)                    │
│  ③ 点击"选课" → 三重校验:                                    │
│     a.容量校验: current_count < max_capacity                 │
│     b.重复校验: 同一学生同一课程同一学期不可重复选               │
│     c.时间冲突校验: 新课与已选课表的节次+周次不可重叠             │
│  ④ 校验通过 → 写入 course_selection + 更新 current_count      │
│                                                              │
│  【退选流程】                                                  │
│  ① 在"我的选课"中点击"退选"                                    │
│  ② 检查是否在允许退选的时间窗口内                               │
│  ③ 更新 selection.status = 2(退选) + current_count - 1       │
│                                                              │
│  【教务容量管理】                                              │
│  ① 查看各课程已选/容量                                         │
│  ② 动态调整 max_capacity / min_capacity                      │
│  ③ 对选课人数不足 min_capacity 的课程发出"可能停开"提示         │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 完整选课业务逻辑

```java
@Service
public class CourseSelectionServiceImpl
        extends ServiceImpl<CourseSelectionMapper, CourseSelection>
        implements ICourseSelectionService {

    @Autowired CourseSelectionMapper selectionMapper;
    @Autowired CourseCapacityMapper capacityMapper;
    @Autowired ScheduleMapper scheduleMapper;
    @Autowired CourseMapper courseMapper;

    /**
     * 学生选课 —— 完整流程 + 事务
     */
    @Override
    @Transactional  // 确保容量更新和选课记录同时成功
    public CommonResult selectCourse(Long studentId, Long courseId, String semester) {

        // ====== 第1步：容量校验 ======
        LambdaQueryWrapper<CourseCapacity> capWrapper = new LambdaQueryWrapper<>();
        capWrapper.eq(CourseCapacity::getCourseId, courseId)
                   .eq(CourseCapacity::getSemester, semester);
        CourseCapacity capacity = capacityMapper.selectOne(capWrapper);

        if (capacity == null) {
            return CommonResult.error(910, "该课程本学期未开课");
        }
        if (capacity.getCurrentCount() >= capacity.getMaxCapacity()) {
            return CommonResult.error(911, "该课程已选满（"
                + capacity.getCurrentCount() + "/" + capacity.getMaxCapacity() + "），请选择其他课程");
        }

        // ====== 第2步：重复选课校验 ======
        LambdaQueryWrapper<CourseSelection> selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(CourseSelection::getStudentId, studentId)
                   .eq(CourseSelection::getCourseId, courseId)
                   .eq(CourseSelection::getSemester, semester)
                   .eq(CourseSelection::getStatus, 1);  // 1=已选
        if (selectionMapper.selectCount(selWrapper) > 0) {
            return CommonResult.error(912, "你已经选过该课程，请勿重复选择");
        }

        // ====== 第3步：时间冲突校验 ======
        // 查询该课程的时间安排
        LambdaQueryWrapper<Schedule> newSchWrapper = new LambdaQueryWrapper<>();
        newSchWrapper.eq(Schedule::getCourseId, courseId)
                      .eq(Schedule::getSemester, semester);
        List<Schedule> newSchedules = scheduleMapper.selectList(newSchWrapper);

        // 查询学生已有课表
        List<Schedule> existingSchedules = scheduleMapper.selectByStudent(studentId, semester);

        // 逐条比对节次+周次是否重叠
        for (Schedule existing : existingSchedules) {
            for (Schedule newSch : newSchedules) {
                if (isTimeOverlap(existing, newSch)) {
                    // 找出冲突的课程名
                    CourseEntity conflictCourse = courseMapper.selectById(existing.getCourseId());
                    return CommonResult.error(913,
                        "与已选课程《" + conflictCourse.getCourseName() + "》时间冲突！"
                        + "（星期" + existing.getWeekDay()
                        + " 第" + existing.getStartPeriod() + "-" + existing.getEndPeriod() + "节）");
                }
            }
        }

        // ====== 第4步：执行选课 ======
        // 4a. 写入选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        selection.setSemester(semester);
        selection.setStatus(1);
        selection.setSelectTime(new Date());
        // 关联排课ID（如果有多个排课时间，取第一个）
        if (!newSchedules.isEmpty()) {
            selection.setScheduleId(newSchedules.get(0).getScheduleId());
        }
        selectionMapper.insert(selection);

        // 4b. 更新课程已选人数
        capacity.setCurrentCount(capacity.getCurrentCount() + 1);
        capacityMapper.updateById(capacity);

        System.out.println("学生 " + studentId + " 选课成功：" + courseId);
        return CommonResult.success();
    }

    /**
     * 判断两个排课时间段是否重叠
     * 重叠条件：星期相同 + 周次有交集 + 节次有交集
     */
    private boolean isTimeOverlap(Schedule a, Schedule b) {
        if (!a.getWeekDay().equals(b.getWeekDay())) return false;
        // 周次交集
        boolean weekOverlap = a.getStartWeek() <= b.getEndWeek()
                           && a.getEndWeek() >= b.getStartWeek();
        // 节次交集
        boolean periodOverlap = a.getStartPeriod() < b.getEndPeriod()
                             && a.getEndPeriod() > b.getStartPeriod();
        return weekOverlap && periodOverlap;
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
            return CommonResult.error(914, "未找到该选课记录");
        }

        // 更新选课状态为"退选"
        selection.setStatus(2);
        selectionMapper.updateById(selection);

        // 容量 -1
        LambdaQueryWrapper<CourseCapacity> capWrapper = new LambdaQueryWrapper<>();
        capWrapper.eq(CourseCapacity::getCourseId, courseId)
                   .eq(CourseCapacity::getSemester, semester);
        CourseCapacity capacity = capacityMapper.selectOne(capWrapper);
        if (capacity != null && capacity.getCurrentCount() > 0) {
            capacity.setCurrentCount(capacity.getCurrentCount() - 1);
            capacityMapper.updateById(capacity);
        }

        return CommonResult.success();
    }

    /**
     * 教务查看某课程的选课学生名单
     */
    @Override
    public CommonResult getStudentListByCourse(Long courseId, String semester) {
        // 直接用 Mapper XML 关联查询 student 表
        List<JSONObject> studentList = selectionMapper.selectStudentListByCourse(courseId, semester);
        return CommonResult.success(studentList);
    }
}
```

### 2.3 Mapper XML（选课学生名单多表关联）

```xml
<!-- CourseSelectionMapper.xml -->
<select id="selectStudentListByCourse" resultType="java.util.Map">
    SELECT
        s.student_id, s.student_name, s.student_no,
        g.grade_name,
        cs.select_time, cs.status as selection_status
    FROM course_selection cs
    INNER JOIN student s ON cs.student_id = s.student_id
    LEFT JOIN grade g ON s.grade_id = g.grade_id
    WHERE cs.course_id = #{courseId}
      AND cs.semester = #{semester}
    ORDER BY cs.select_time
</select>
```

### 2.4 前端实现要点

**选课卡片**需要展示的关键信息：
- 课程名称、授课教师、学分
- 上课时间（星期几 + 第几节）
- 上课教室
- **已选人数 / 最大容量**（进度条 `el-progress`）
- "选课"/"退选" 按钮（根据是否已选动态切换）

**选课列表过滤功能**：
- 按院系筛选
- 按课程类型筛选（必修/选修/公选）
- 按上课时间筛选（仅显示无冲突课程）
- 搜索框按课程名/教师名搜索

---

## A3：成绩评定与预警系统

### 3.1 业务闭环详解

```
┌──────────────────────────────────────────────────────────────┐
│                     成绩评定与预警                             │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  【成绩录入流程】                                              │
│  ① 教师选择学期 + 所授课程                                     │
│  ② 系统加载该课程选课学生列表                                   │
│  ③ 教师逐行录入/修改分数（el-table 可编辑模式）                  │
│  ④ 支持 Excel 批量导入（使用 Apache POI 或前端 xlsx 解析）      │
│  ⑤ 提交后:                                                    │
│     a.自动计算每门课的绩点(GPA)                                  │
│     b.标注 status: 0=不及格(score<60), 1=正常                    │
│     c.触发学业预警检查                                         │
│                                                              │
│  【绩点计算规则】                                              │
│  ┌──────────┬──────┐                                         │
│  │ 分数区间   │ 绩点  │                                         │
│  ├──────────┼──────┤                                         │
│  │ 90-100   │ 4.0  │                                         │
│  │ 80-89    │ 3.0  │                                         │
│  │ 70-79    │ 2.0  │                                         │
│  │ 60-69    │ 1.0  │                                         │
│  │ <60      │ 0.0  │                                         │
│  └──────────┴──────┘                                         │
│                                                              │
│  【学业预警规则】                                              │
│  ① 统计当前学期不及格课程数量                                    │
│  ② ≥3门不及格 → 黄色预警（通知学生 + 辅导员）                    │
│  ③ 累计不及格学分 ≥10 → 橙色预警（留级风险）                    │
│  ④ 累计不及格学分 ≥20 → 红色预警（退学风险）                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 完整成绩录入逻辑

```java
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, ScoreEntity>
        implements IScoreService {

    @Autowired ScoreMapper scoreMapper;
    @Autowired CourseSelectionMapper selectionMapper;
    @Autowired StudentMapper studentMapper;

    /**
     * 教师单个录入/修改成绩
     */
    @Override
    public CommonResult inputScore(ScoreEntity score) {
        // 1. 校验学生是否选了这门课
        LambdaQueryWrapper<CourseSelection> selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(CourseSelection::getStudentId, score.getStudentId())
                   .eq(CourseSelection::getCourseId, score.getCourseId())
                   .eq(CourseSelection::getSemester, score.getSemester())
                   .eq(CourseSelection::getStatus, 1);
        if (selectionMapper.selectCount(selWrapper) == 0) {
            return CommonResult.error(920, "该学生未选修此课程");
        }

        // 2. 校验分数范围
        if (score.getScoreScore() < 0 || score.getScoreScore() > 100) {
            return CommonResult.error(921, "分数范围必须在 0-100 之间");
        }

        // 3. 计算绩点
        BigDecimal gpa = calculateGPA(score.getScoreScore());
        score.setGpa(gpa);

        // 4. 标注是否不及格
        score.setStatus(score.getScoreScore() >= 60 ? 1 : 0);

        // 5. 检查是否已有成绩（有则更新，无则新增）
        LambdaQueryWrapper<ScoreEntity> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ScoreEntity::getStudentId, score.getStudentId())
                    .eq(ScoreEntity::getCourseId, score.getCourseId())
                    .eq(ScoreEntity::getSemester, score.getSemester());
        ScoreEntity exist = scoreMapper.selectOne(existWrapper);

        if (exist != null) {
            score.setScoreId(exist.getScoreId());
            scoreMapper.updateById(score);
        } else {
            scoreMapper.insert(score);
        }

        // 6. 触发学业预警检查
        checkAcademicWarning(score.getStudentId(), score.getSemester());

        System.out.println("成绩录入: 学生" + score.getStudentId()
            + " 课程" + score.getCourseId() + " 分数" + score.getScoreScore()
            + " 绩点" + gpa);
        return CommonResult.success();
    }

    /**
     * 绩点计算
     * 90-100 → 4.0, 80-89 → 3.0, 70-79 → 2.0, 60-69 → 1.0, <60 → 0.0
     */
    private BigDecimal calculateGPA(int score) {
        if (score >= 90) return new BigDecimal("4.0");
        else if (score >= 80) return new BigDecimal("3.0");
        else if (score >= 70) return new BigDecimal("2.0");
        else if (score >= 60) return new BigDecimal("1.0");
        else return BigDecimal.ZERO;
    }

    /**
     * 学业预警检查
     *
     * 三级预警体系：
     * - 黄色：当前学期 ≥3 门不及格
     * - 橙色：累计不及格学分 ≥10（留级风险）
     * - 红色：累计不及格学分 ≥20（退学风险）
     */
    private void checkAcademicWarning(Long studentId, String semester) {
        // 当前学期不及格数量
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEntity::getStudentId, studentId)
               .eq(ScoreEntity::getSemester, semester)
               .eq(ScoreEntity::getStatus, 0);  // 0=不及格
        long currentFailCount = scoreMapper.selectCount(wrapper);

        // 累计不及格数量
        LambdaQueryWrapper<ScoreEntity> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(ScoreEntity::getStudentId, studentId)
                  .eq(ScoreEntity::getStatus, 0);
        long totalFailCount = scoreMapper.selectCount(allWrapper);

        String level = null;
        String message = null;
        if (totalFailCount >= 20) {
            level = "红色";
            message = "累计不及格课程达" + totalFailCount + "门，已达到退学警示线！请尽快联系辅导员。";
        } else if (totalFailCount >= 10) {
            level = "橙色";
            message = "累计不及格课程" + totalFailCount + "门，存在留级风险。请制定补考/重修计划。";
        } else if (currentFailCount >= 3) {
            level = "黄色";
            message = "本学期有" + currentFailCount + "门课程不及格，请合理安排复习时间。";
        }

        if (level != null) {
            // 保存预警通知（调用 notification 表，可复用 C 成员的 Notification 系统）
            System.err.println("【" + level + "预警】学生ID:" + studentId + " → " + message);
            // TODO: 写入 notification 表 + 推送给辅导员
        }
    }

    /**
     * 学生按学期查询个人成绩单（含 GPA 汇总）
     */
    @Override
    public JSONObject getStudentScoreReport(Long studentId, String semester) {
        LambdaQueryWrapper<ScoreEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEntity::getStudentId, studentId);
        if (semester != null) {
            wrapper.eq(ScoreEntity::getSemester, semester);
        }
        List<ScoreEntity> scores = scoreMapper.selectList(wrapper);

        JSONObject report = new JSONObject();
        JSONArray detailArray = new JSONArray();
        int totalScore = 0;
        BigDecimal totalGpa = BigDecimal.ZERO;
        int passCount = 0;
        int failCount = 0;

        for (ScoreEntity s : scores) {
            JSONObject item = new JSONObject();
            item.put("courseId", s.getCourseId());
            item.put("score", s.getScoreScore());
            item.put("gpa", s.getGpa());
            item.put("semester", s.getSemester());
            item.put("status", s.getStatus() == 1 ? "及格" : "不及格");
            detailArray.add(item);

            totalScore += s.getScoreScore();
            totalGpa = totalGpa.add(s.getGpa());
            if (s.getStatus() == 1) passCount++; else failCount++;
        }

        report.put("details", detailArray);
        report.put("averageScore", scores.isEmpty() ? 0 : totalScore / scores.size());
        report.put("averageGpa", scores.isEmpty() ? 0 : totalGpa.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP));
        report.put("passCount", passCount);
        report.put("failCount", failCount);
        report.put("totalCount", scores.size());

        return report;
    }
}
```

### 3.3 前端实现要点

**成绩录入页** (`ScoreInput.vue`)：
- `el-table` 设置为可编辑模式，分数列用 `el-input-number`（范围 0-100）
- 支持 Tab 键快速切换到下一行
- 提交前显示"确认提交？提交后不可修改"的 `ElMessageBox.confirm`
- 支持 Excel 批量导入（使用 `xlsx` 库解析，映射学号→分数）
- 顶部筛选：学期下拉 + 课程下拉

**成绩查询页** (`ScoreView.vue`)：
- 顶部汇总卡片：平均分、平均绩点、及格门数、不及格门数（四色卡片）
- 下方表格：课程名、分数、绩点、状态标签（el-tag: 绿色及格/红色不及格）
- 绩点趋势图：多学期折线图（ECharts）

---

## A4：考试安排与重修补考系统

### 4.1 业务闭环详解

```
┌──────────────────────────────────────────────────────────────┐
│                   考试安排与重修补考                            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  【考试发布流程(教务)】                                        │
│  ① 选择学期 + 课程                                             │
│  ② 设置考试名称(如: 2025秋-高等数学期末)                        │
│  ③ 设置考试类型(期末考试/补考/重修考试)                          │
│  ④ 选择考试日期 + 开始时间 + 结束时间                           │
│  ⑤ 分配考试教室(支持多教室)                                    │
│  ⑥ 为每个教室分配座位(按学号排序/随机)                          │
│  ⑦ 为每个教室安排监考教师(主监考/副监考)                        │
│                                                              │
│  【学生查询流程】                                              │
│  ① 查看"我的考试"列表(按学期/时间排序)                          │
│  ② 查看每场考试的: 日期/时间/教室/座位号                         │
│  ③ 不及格课程自动显示"补考报名"入口                             │
│                                                              │
│  【补考/重修报名流程】                                         │
│  ① 学生查看不及格课程列表                                       │
│  ② 点击"报名补考" → 系统校验:                                  │
│     a.该课程成绩是否确实 < 60                                  │
│     b.该课程是否已安排补考                                      │
│     c.是否在补考报名时间窗口内                                   │
│  ③ 报名成功 → 等待教务审核                                      │
│  ④ 教务审核通过 → 安排补考时间/教室                             │
│                                                              │
│  【监考安排】                                                   │
│  ① 教务为每场考试分配监考教师                                   │
│  ② 校验: 同一教师不能在同一时间监考两场                          │
│  ③ 教师端查看"我的监考任务"                                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 4.2 核心算法：考试座位自动分配

```java
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    @Autowired ExamMapper examMapper;
    @Autowired ExamRoomMapper examRoomMapper;
    @Autowired CourseSelectionMapper selectionMapper;
    @Autowired InvigilationMapper invigilationMapper;
    @Autowired ClassroomMapper classroomMapper;

    /**
     * 自动分配考试座位
     *
     * 算法：
     * 1. 查询该考试对应的所有选课学生
     * 2. 按学号升序排列
     * 3. 分配到指定教室，座位号 = 行号+列号（如 3排5座）
     * 4. 如果学生数 > 教室容量，自动分配到多个教室
     */
    @Override
    @Transactional
    public CommonResult autoAssignSeats(Long examId) {
        Exam exam = this.getById(examId);
        if (exam == null) return CommonResult.error(930, "考试不存在");

        // 1. 获取选课学生列表
        List<Map<String, Object>> students = selectionMapper
            .selectStudentListByCourse(exam.getCourseId(), exam.getSemester());

        if (students.isEmpty()) return CommonResult.error(931, "该课程无选课学生");

        // 2. 获取分配给该考试的教室
        LambdaQueryWrapper<ExamRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(ExamRoom::getExamId, examId);
        roomWrapper.isNull(ExamRoom::getStudentId);  // 尚未分配座位的记录
        List<ExamRoom> rooms = examRoomMapper.selectList(roomWrapper);

        if (rooms.isEmpty()) {
            return CommonResult.error(932, "请先分配考试教室");
        }

        // 3. 计算每个教室分配多少学生
        int totalCapacity = 0;
        for (ExamRoom r : rooms) {
            Classroom cr = classroomMapper.selectById(r.getClassroomId());
            totalCapacity += cr.getCapacity();
        }

        if (students.size() > totalCapacity) {
            return CommonResult.error(933,
                "教室容量不足！需要" + students.size() + "座，仅" + totalCapacity + "座");
        }

        // 4. 按教室分配学生，座位号格式：排号+座位号
        int studentIndex = 0;
        for (ExamRoom room : rooms) {
            Classroom cr = classroomMapper.selectById(room.getClassroomId());
            int seatsInThisRoom = Math.min(cr.getCapacity(), students.size() - studentIndex);

            for (int i = 0; i < seatsInThisRoom; i++) {
                Map<String, Object> student = students.get(studentIndex + i);

                ExamRoom seat = new ExamRoom();
                seat.setExamRoomId(room.getExamRoomId());  // 更新已有记录
                seat.setExamId(examId);
                seat.setClassroomId(room.getClassroomId());
                seat.setStudentId((Long) student.get("student_id"));
                // 座位号: 如 03-05 表示第3排第5座
                int row = (i / 8) + 1;   // 每排8人
                int col = (i % 8) + 1;
                seat.setSeatNo(String.format("%02d-%02d", row, col));

                examRoomMapper.updateById(seat);
            }
            studentIndex += seatsInThisRoom;
        }

        System.out.println("考试 " + exam.getExamName() + " 座位分配完成，共 " + students.size() + " 名考生");
        return CommonResult.success();
    }

    /**
     * 监考冲突校验
     */
    @Override
    public CommonResult assignInvigilation(Invigilation inv) {
        // 查询该教师已有的监考安排
        LambdaQueryWrapper<Invigilation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invigilation::getTeacherId, inv.getTeacherId());
        List<Invigilation> existing = invigilationMapper.selectList(wrapper);

        // 逐条比对时间是否冲突
        for (Invigilation exist : existing) {
            Exam existExam = examMapper.selectById(exist.getExamId());
            Exam newExam = examMapper.selectById(inv.getExamId());

            if (existExam.getExamDate().equals(newExam.getExamDate())) {
                // 同一天，检查时间段是否重叠
                if (existExam.getStartTime().before(newExam.getEndTime()) &&
                    newExam.getStartTime().before(existExam.getEndTime())) {
                    return CommonResult.error(934,
                        "该教师在 " + existExam.getExamDate()
                        + " 已有监考任务（" + existExam.getExamName() + "），时间冲突！");
                }
            }
        }

        invigilationMapper.insert(inv);
        return CommonResult.success();
    }

    /**
     * 补考/重修报名
     */
    @Override
    public CommonResult applyResit(ResitApply apply) {
        // 校验：该学生该课程是否确实不及格
        LambdaQueryWrapper<ScoreEntity> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(ScoreEntity::getStudentId, apply.getStudentId())
                    .eq(ScoreEntity::getCourseId, apply.getCourseId())
                    .eq(ScoreEntity::getStatus, 0);  // 不及格
        if (scoreMapper.selectCount(scoreWrapper) == 0) {
            return CommonResult.error(935, "该课程成绩已及格，无需补考");
        }

        // 校验：是否已有进行中的补考报名
        LambdaQueryWrapper<ResitApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(ResitApply::getStudentId, apply.getStudentId())
                    .eq(ResitApply::getCourseId, apply.getCourseId())
                    .in(ResitApply::getStatus, 0, 1);  // 待审核或已通过
        if (resitApplyMapper.selectCount(applyWrapper) > 0) {
            return CommonResult.error(936, "你已有该课程的补考/重修申请，请勿重复提交");
        }

        apply.setStatus(0);
        apply.setApplyTime(new Date());
        resitApplyMapper.insert(apply);
        return CommonResult.success();
    }
}
```

---

## A5：毕业设计过程管理系统

### 5.1 状态机设计

```
┌─────────────────────────────────────────────────────────────┐
│                    毕设管理状态流转                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  【课题状态】                                                 │
│  教师发布 → 可选(1) → 学生已选满 → 已满(0) → 学期结束 → 结束(-1)│
│                                                             │
│  【选题状态】                                                 │
│  学生申请 → 待确认(0) → 教师通过 → 已确认(1)                   │
│                       → 教师拒绝 → 已拒绝(2) → 学生可重新选    │
│                                                             │
│  【报告提交流程】                                              │
│  开题报告 → 中期报告 → 论文初稿 → 最终稿                        │
│  (每个阶段: 学生提交 → 教师批注/评分 → 学生根据反馈修改 → 重新提交)│
│                                                             │
│  【评分机制】                                                 │
│  开题报告占比 10% + 中期报告占比 20% + 论文质量占比 50%         │
│  + 答辩表现占比 20% = 最终成绩                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 完整服务实现

```java
@Service
public class GraduationServiceImpl
        extends ServiceImpl<GraduationTopicMapper, GraduationTopic>
        implements IGraduationService {

    @Autowired GraduationTopicMapper topicMapper;
    @Autowired GraduationSelectionMapper selectionMapper;
    @Autowired GraduationReportMapper reportMapper;
    @Autowired UserMapper userMapper;

    /**
     * 教师发布课题
     */
    @Override
    public CommonResult publishTopic(GraduationTopic topic) {
        topic.setCurrentStudent(0);
        topic.setStatus(1);  // 可选
        topicMapper.insert(topic);
        return CommonResult.success();
    }

    /**
     * 学生选题 —— 含完整校验
     */
    @Override
    @Transactional
    public CommonResult selectTopic(Long studentId, Long topicId) {
        // 1. 课题状态校验
        GraduationTopic topic = topicMapper.selectById(topicId);
        if (topic == null) return CommonResult.error(940, "课题不存在");
        if (topic.getStatus() != 1) return CommonResult.error(941, "该课题当前不可选（原因：已满或已结束）");
        if (topic.getCurrentStudent() >= topic.getMaxStudent()) {
            return CommonResult.error(942, "该课题名额已满（" + topic.getCurrentStudent() + "/" + topic.getMaxStudent() + "）");
        }

        // 2. 学生是否已选题（一个学生只能选一个课题）
        LambdaQueryWrapper<GraduationSelection> selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(GraduationSelection::getStudentId, studentId)
                   .eq(GraduationSelection::getStatus, 1);  // 已确认的
        if (selectionMapper.selectCount(selWrapper) > 0) {
            return CommonResult.error(943, "你已选题成功！如需要更换，请先联系当前导师取消");
        }
        // 待确认的申请也需要检查
        selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(GraduationSelection::getStudentId, studentId)
                   .eq(GraduationSelection::getStatus, 0);
        if (selectionMapper.selectCount(selWrapper) > 0) {
            return CommonResult.error(944, "你已有一个待确认的选题申请，请等待教师处理");
        }

        // 3. 执行选题（状态=待确认）
        GraduationSelection sel = new GraduationSelection();
        sel.setTopicId(topicId);
        sel.setStudentId(studentId);
        sel.setStatus(0);
        sel.setSelectTime(new Date());
        selectionMapper.insert(sel);

        // 4. 预占名额（待确认状态也占位，防止超额）
        topic.setCurrentStudent(topic.getCurrentStudent() + 1);
        if (topic.getCurrentStudent() >= topic.getMaxStudent()) {
            topic.setStatus(0);  // 自动标记为已满
        }
        topicMapper.updateById(topic);

        return CommonResult.success();
    }

    /**
     * 教师确认/拒绝选题
     */
    @Override
    @Transactional
    public CommonResult confirmSelection(Long selectId, Integer status) {
        GraduationSelection sel = selectionMapper.selectById(selectId);
        if (sel == null) return CommonResult.error(945, "选题记录不存在");

        sel.setStatus(status);  // 1=已确认, 2=已拒绝
        selectionMapper.updateById(sel);

        // 如果拒绝，释放课题名额
        if (status == 2) {
            GraduationTopic topic = topicMapper.selectById(sel.getTopicId());
            topic.setCurrentStudent(Math.max(0, topic.getCurrentStudent() - 1));
            topic.setStatus(1);  // 恢复可选状态
            topicMapper.updateById(topic);
        }

        return CommonResult.success();
    }

    /**
     * 学生提交报告
     */
    @Override
    public CommonResult submitReport(GraduationReport report) {
        // 校验选题是否已确认
        LambdaQueryWrapper<GraduationSelection> selWrapper = new LambdaQueryWrapper<>();
        selWrapper.eq(GraduationSelection::getStudentId, report.getStudentId())
                   .eq(GraduationSelection::getTopicId, report.getTopicId())
                   .eq(GraduationSelection::getStatus, 1);
        if (selectionMapper.selectCount(selWrapper) == 0) {
            return CommonResult.error(946, "你的选题尚未被教师确认，无法提交报告");
        }

        // 校验报告类型顺序（不能跳过开题直接交论文）
        String[] order = {"开题报告", "中期报告", "论文初稿", "最终稿"};
        int currentIndex = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(report.getReportType())) { currentIndex = i; break; }
        }
        if (currentIndex > 0) {
            // 检查上一阶段是否已提交
            LambdaQueryWrapper<GraduationReport> prevWrapper = new LambdaQueryWrapper<>();
            prevWrapper.eq(GraduationReport::getStudentId, report.getStudentId())
                       .eq(GraduationReport::getTopicId, report.getTopicId())
                       .eq(GraduationReport::getReportType, order[currentIndex - 1]);
            if (reportMapper.selectCount(prevWrapper) == 0) {
                return CommonResult.error(947, "请先提交《" + order[currentIndex - 1] + "》");
            }
        }

        report.setSubmitTime(new Date());
        reportMapper.insert(report);
        return CommonResult.success();
    }

    /**
     * 教师批注 + 评分
     */
    @Override
    public CommonResult giveFeedback(Long reportId, String feedback, Integer score) {
        GraduationReport report = reportMapper.selectById(reportId);
        if (report == null) return CommonResult.error(948, "报告不存在");

        report.setTeacherFeedback(feedback);
        report.setFeedbackTime(new Date());
        report.setScore(score);
        reportMapper.updateById(report);

        // 如果评分为最终稿，计算最终成绩
        if ("最终稿".equals(report.getReportType()) && score != null) {
            calculateFinalScore(report.getStudentId(), report.getTopicId());
        }

        return CommonResult.success();
    }

    /**
     * 计算毕业设计最终成绩
     */
    private void calculateFinalScore(Long studentId, Long topicId) {
        LambdaQueryWrapper<GraduationReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GraduationReport::getStudentId, studentId)
               .eq(GraduationReport::getTopicId, topicId);
        List<GraduationReport> reports = reportMapper.selectList(wrapper);

        double finalScore = 0;
        for (GraduationReport r : reports) {
            if (r.getScore() == null) continue;
            switch (r.getReportType()) {
                case "开题报告": finalScore += r.getScore() * 0.10; break;
                case "中期报告": finalScore += r.getScore() * 0.20; break;
                case "论文初稿": finalScore += r.getScore() * 0.20; break;
                case "最终稿":  finalScore += r.getScore() * 0.50; break;
            }
        }
        System.out.println("学生 " + studentId + " 毕设最终成绩: " + finalScore);
    }
}
```

---

## A6：AI 智能学习助理 :bulb:

### 6.1 三个 AI 能力的完整实现

```java
@Service
public class AIStudyService {

    @Autowired DashScopeChatModel dashScopeChatModel;
    @Autowired StudyMaterialMapper materialMapper;
    @Autowired AiStudyRecordMapper recordMapper;
    @Autowired ScoreMapper scoreMapper;
    @Autowired CourseSelectionMapper selectionMapper;

    // ============ 能力1：知识点摘要生成 ============

    /**
     * 完整流程：
     * 1. 读取学习资料内容
     * 2. 构造结构化 Prompt 发送给大模型
     * 3. 解析/存储 AI 返回结果
     * 4. 返回给前端展示
     */
    public JSONObject generateSummary(Long materialId) {
        StudyMaterial material = materialMapper.selectById(materialId);
        if (material == null) return errorResult("学习资料不存在");

        String fileContent = readFileContent(material.getFileUrl());
        if (fileContent == null || fileContent.trim().isEmpty()) {
            return errorResult("文件内容为空，无法生成摘要");
        }

        // 截断过长的内容（模型有 token 限制）
        String truncatedContent = fileContent.length() > 8000
            ? fileContent.substring(0, 8000) + "...(内容已截断)" : fileContent;

        String prompt = """
            你是智慧校园的 AI 学习助手。请根据以下学习资料，生成一份结构化的知识点摘要。

            【输出要求】
            1. **核心知识点**（3-5个）：每个用一句话概括
            2. **详细解释**：每个知识点用2-3句话展开说明
            3. **重点标注**：标注哪些是需要重点掌握的（用 ⭐ 标记）
            4. **难点提示**：标注哪些是容易混淆或难以理解的（用 :warning: 标记）
            5. **记忆口诀**：为核心概念创建一个简短易记的口诀

            【学习资料内容】
            %s

            【输出格式】请使用 Markdown 格式输出。
            """.formatted(truncatedContent);

        String result = dashScopeChatModel.call(prompt);

        // 保存记录
        saveRecord(material.getStudentId(), materialId, "summary", "生成知识点摘要", result);

        JSONObject resp = new JSONObject();
        resp.put("summary", result);
        resp.put("materialName", material.getFileName());
        return resp;
    }

    // ============ 能力2：智能复习题生成 ============

    public JSONObject generateQuestions(Long materialId) {
        StudyMaterial material = materialMapper.selectById(materialId);
        if (material == null) return errorResult("学习资料不存在");

        String fileContent = readFileContent(material.getFileUrl());
        String truncatedContent = fileContent.length() > 6000
            ? fileContent.substring(0, 6000) + "...(内容已截断)" : fileContent;

        String prompt = """
            你是智慧校园的 AI 出题专家。请根据以下学习资料，生成一套复习题。

            【出题要求】
            1. **选择题**（3道）：每题4个选项，标注正确答案和解析
            2. **填空题**（2道）：每空给出标准答案
            3. **简答题**（2道）：给出参考答案要点（每条50字以内）
            4. 题目覆盖资料中的**核心知识点**
            5. 难度递进：基础 → 进阶 → 综合

            【学习资料内容】
            %s

            【输出格式】Markdown，每道题之间用 --- 分割。
            """.formatted(truncatedContent);

        String result = dashScopeChatModel.call(prompt);

        saveRecord(material.getStudentId(), materialId, "question", "生成复习题", result);

        JSONObject resp = new JSONObject();
        resp.put("questions", result);
        resp.put("materialName", material.getFileName());
        return resp;
    }

    // ============ 能力3：自适应学习路径推荐 ============

    public JSONObject recommendStudyPath(Long studentId) {
        // 1. 收集学生数据
        List<ScoreEntity> scores = scoreMapper.selectByStudentId(studentId);
        List<Map<String, Object>> selections = selectionMapper.selectByStudentWithCourse(studentId);

        // 2. 分析优势和薄弱科目
        JSONArray strengthArr = new JSONArray();
        JSONArray weakArr = new JSONArray();
        for (ScoreEntity s : scores) {
            JSONObject item = new JSONObject();
            item.put("courseId", s.getCourseId());
            item.put("score", s.getScoreScore());
            item.put("gpa", s.getGpa());
            if (s.getScoreScore() >= 80) strengthArr.add(item);
            else if (s.getScoreScore() < 70) weakArr.add(item);
        }

        // 3. 构造 Prompt
        String prompt = """
            你是智慧校园的 AI 学习规划师。根据以下学生数据，制定个性化学习路径。

            【学生学业数据】
            优势科目（≥80分）：%s
            薄弱科目（<70分）：%s
            已选课程：%s

            【推荐要求】
            1. **薄弱科目提升计划**：针对每门薄弱科目，给出具体的学习方法建议
            2. **每日学习时间分配**：按优先级分配每天的学习时间
            3. **下学期选课建议**：基于现有基础，推荐2-3门适合的进阶课程
            4. **学习资源推荐**：推荐适合的学习网站/书籍/学习方法
            5. **目标设定**：为下个学期设定可量化的成绩目标

            【输出格式】Markdown，分段落，条理清晰。
            """.formatted(
                JSON.toJSONString(strengthArr),
                JSON.toJSONString(weakArr),
                JSON.toJSONString(selections)
            );

        String result = dashScopeChatModel.call(prompt);

        saveRecord(studentId, null, "path_recommend", "学习路径推荐", result);

        JSONObject resp = new JSONObject();
        resp.put("recommendation", result);
        resp.put("strengths", strengthArr);
        resp.put("weaknesses", weakArr);
        return resp;
    }

    // ============ 工具方法 ============

    /** 读取文件内容（简化实现——根据实际文件存储方式调整） */
    private String readFileContent(String fileUrl) {
        // TODO: 根据文件存储方式实现（本地文件 / OSS / 数据库）
        // 示例：如果是文本文件，用 Files.readString()；如果是 PDF/Word，用 Apache Tika 解析
        return "[文件内容读取中...]";
    }

    /** 保存 AI 调用记录 */
    private void saveRecord(Long studentId, Long materialId,
                            String recordType, String prompt, String response) {
        AiStudyRecord record = new AiStudyRecord();
        record.setStudentId(studentId);
        record.setMaterialId(materialId);
        record.setRecordType(recordType);
        record.setPrompt(prompt);
        record.setAiResponse(response);
        record.setCreateTime(new Date());
        recordMapper.insert(record);
    }

    private JSONObject errorResult(String msg) {
        JSONObject obj = new JSONObject();
        obj.put("error", msg);
        return obj;
    }

    // ============ 流式对话（可选增强） ============

    /**
     * AI 答疑——流式对话
     * 学生可以向 AI 提问学习相关问题，AI 实时流式回答
     */
    public Flux<String> streamTutorChat(String sessionId, String question) {
        // 构建系统提示
        String systemPrompt = """
            你是智慧校园的 AI 学习导师。请：
            1. 用通俗易懂的语言回答学生的问题
            2. 如果问题涉及课程知识点，提及相关的概念和公式
            3. 回答最后给出一个相关的思考题
            4. 保持友好、鼓励的语气
            """;

        // 使用 ChatModelService 的模式（参照示例中的流式对话实现）
        List<Message> history = conversationHistory
            .computeIfAbsent(sessionId, k -> {
                List<Message> msgs = new ArrayList<>();
                msgs.add(new AssistantMessage(systemPrompt));
                return msgs;
            });

        history.add(new UserMessage(question));
        Prompt prompt = new Prompt(history);

        StringBuilder fullResponse = new StringBuilder();
        return dashScopeChatModel.stream(prompt)
            .map(response -> {
                if (response.getResult() != null && response.getResult().getOutput() != null) {
                    String text = response.getResult().getOutput().getText();
                    if (text != null) fullResponse.append(text);
                    return text;
                }
                return "";
            })
            .doOnComplete(() -> {
                if (fullResponse.length() > 0) {
                    history.add(new AssistantMessage(fullResponse.toString()));
                }
            })
            .doOnError(error -> {
                if (!history.isEmpty()) history.remove(history.size() - 1);
            });
    }
    private Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
}
```

### 6.2 前端实现要点

```vue
<template>
  <div class="ai-study-page">
    <el-tabs v-model="activeTab">
      <!-- Tab1: 知识点摘要 -->
      <el-tab-pane label="知识点摘要" name="summary">
        <el-upload :on-success="onUploadSuccess" action="/teaching/ai/material/upload">
          <el-button type="primary">上传课件/大纲</el-button>
        </el-upload>
        <el-table :data="materialList" @row-click="selectMaterial" highlight-current-row>
          <el-table-column prop="fileName" label="文件名" />
          <el-table-column prop="uploadTime" label="上传时间" />
        </el-table>
        <el-button @click="doGenerateSummary" :loading="loading" :disabled="!selectedMaterial">
          生成知识点摘要
        </el-button>
        <div v-if="summaryResult" class="ai-result" v-html="renderMarkdown(summaryResult)" />
      </el-tab-pane>

      <!-- Tab2: 复习题 -->
      <el-tab-pane label="复习题生成" name="questions">
        <!-- 同上，选择资料 → 生成复习题 -->
      </el-tab-pane>

      <!-- Tab3: 学习路径 -->
      <el-tab-pane label="学习路径推荐" name="path">
        <el-button @click="doRecommendPath" :loading="pathLoading">
          分析我的学习数据，推荐学习路径
        </el-button>
        <div v-if="pathResult" class="ai-result" v-html="renderMarkdown(pathResult)" />
        <!-- 额外展示：优势科目 + 薄弱科目 卡片 -->
        <el-row v-if="pathData">
          <el-col :span="12">
            <el-card header="优势科目">
              <el-tag v-for="s in pathData.strengths" :key="s.courseId" type="success">
                {{ s.courseName }}: {{ s.score }}分
              </el-tag>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card header="薄弱科目">
              <el-tag v-for="w in pathData.weaknesses" :key="w.courseId" type="danger">
                {{ w.courseName }}: {{ w.score }}分
              </el-tag>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
```

---

## 文件目录总览

```
backend/campus-app/src/main/java/com/smartcampus/app/
├── dao/teaching/
│   ├── ClassroomMapper.java
│   ├── ScheduleMapper.java          + ScheduleMapper.xml
│   ├── ScheduleChangeMapper.java
│   ├── CourseSelectionMapper.java   + CourseSelectionMapper.xml
│   ├── CourseCapacityMapper.java
│   ├── ScoreMapper.java
│   ├── ExamMapper.java
│   ├── ExamRoomMapper.java
│   ├── InvigilationMapper.java
│   ├── ResitApplyMapper.java
│   ├── GraduationTopicMapper.java
│   ├── GraduationSelectionMapper.java
│   ├── GraduationReportMapper.java
│   ├── StudyMaterialMapper.java
│   └── AiStudyRecordMapper.java
├── service/teaching/
│   ├── IClassroomService.java       → ClassroomServiceImpl
│   ├── IScheduleService.java        → ScheduleServiceImpl
│   ├── ICourseSelectionService.java → CourseSelectionServiceImpl
│   ├── IScoreService.java           → ScoreServiceImpl
│   ├── IExamService.java            → ExamServiceImpl
│   ├── IGraduationService.java      → GraduationServiceImpl
│   └── AIStudyService.java          (无需接口)
└── controller/teaching/
    ├── ScheduleController.java       (/teaching/schedule/*)
    ├── CourseSelectionController.java(/teaching/selection/*)
    ├── ScoreController.java          (/teaching/score/*)
    ├── ExamController.java           (/teaching/exam/*)
    ├── GraduationController.java     (/teaching/graduation/*)
    └── AIStudyController.java        (/teaching/ai/*)

frontend/src/platform/src/views/teaching/
├── CourseSchedule.vue     # ①课表查询（学生/教师共用）
├── ScheduleManage.vue     # ①排课管理（教务）+ 调课审批
├── CourseSelection.vue    # ②选课中心（学生）
├── SelectionManage.vue    # ②选课管理（教务/教师）
├── ScoreInput.vue         # ③成绩录入（教师）
├── ScoreView.vue          # ③成绩查询（学生）
├── ExamManage.vue         # ④考试管理（教务）
├── ExamView.vue           # ④我的考试（学生）
├── ResitApply.vue         # ④补考报名（学生）
├── GraduationManage.vue   # ⑤毕设管理（教师）
├── GraduationStudent.vue  # ⑤我的毕设（学生）
└── AIStudy.vue            # ⑥AI学习助理（学生）
```

---

## 实施顺序

| 顺序 | 模块 | 依赖 | 预估工时 | 产出物 |
|------|------|------|----------|--------|
| **1** | A2 选课系统 | course, student, course_capacity | 3天 | 选课/退选/名单 3 接口 + 2 页面 |
| **2** | A3 成绩系统 | score, course_selection | 2天 | 录入/查询/GPA/预警 4 接口 + 2 页面 |
| **3** | A1 排课课表 | classroom, schedule, course | 4天 | 排课CRUD/课表查询/调课 6 接口 + 3 页面 |
| **4** | A4 考试补考 | exam, score（不及格判定） | 3天 | 考试发布/座位/监考/补考 7 接口 + 4 页面 |
| **5** | A5 毕设管理 | graduation_*, student | 3天 | 课题/选题/报告/批注 7 接口 + 4 页面 |
| **6** | A6 AI 助理 | DashScope 配置就绪 | 2天 | 摘要/复习题/路径推荐/流式答疑 + 1 页面 |

> **总计**：约 17 个工作日，31 个后端接口，16 个前端页面。
