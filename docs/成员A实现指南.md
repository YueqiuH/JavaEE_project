# 成员 A 实现指南：教务核心、学业管理与 AI 智能学习助手

> **Controller 包**：`com.smartcampus.app.controller.teaching`
> **Service 包**：`com.smartcampus.app.service.teaching`
> **Mapper 包**：`com.smartcampus.app.dao.teaching`
> **Entity 包**：`com.smartcampus.contract.entity`
> **涉及表**：classroom, schedule, course_selection, exam, exam_room, invigilation, graduation_topic, graduation_report

---

## 开发规范速查

| 项目 | 规范 |
|------|------|
| 实体类 | `@Data` + `@ToString` + `@TableName` + `@TableId(type = IdType.AUTO)` + `implements Serializable` |
| Mapper | `extends BaseMapper<Entity>`，接口定义在 `dao` 包 |
| Service 接口 | `extends IService<Entity>`，命名 `I{Name}Service` |
| Service 实现 | `extends ServiceImpl<Mapper, Entity> implements I{Name}Service`，`@Service` 注解 |
| Controller | `@RestController` + `@RequestMapping("/xxx")`，`@Tag` + `@Operation` 中文注释 |
| 依赖注入 | `@Autowired` 字段注入 |
| 响应封装 | `CommonResult<T>` 统一返回，`CommonResult.success(data)` / `CommonResult.error(ErrorCode)` |
| 分页查询 | MyBatis Plus `Page<Entity>` + `LambdaQueryWrapper` |
| 前端组件 | `<script setup>` 组合式 API，Element Plus 组件库 |
| API 调用 | `@/api/getData.js` 定义函数，`.then(res => { if (res && res != -1) {...} })` |

---

## 数据库表设计

### 1. classroom（教室/资源表）

```sql
CREATE TABLE IF NOT EXISTS `classroom` (
    `classroom_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '教室主键ID',
    `classroom_name` VARCHAR(64)  NOT NULL                COMMENT '教室名称（如：3教201）',
    `building`       VARCHAR(32)  DEFAULT NULL            COMMENT '所在教学楼',
    `capacity`       INT          NOT NULL DEFAULT 0      COMMENT '容纳人数',
    `type`           VARCHAR(16)  DEFAULT '普通教室'      COMMENT '类型：普通教室/多媒体/实验室/阶梯教室',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=可用, 0=维护中',
    PRIMARY KEY (`classroom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室资源表';
```

### 2. schedule（排课/课表表）

```sql
CREATE TABLE IF NOT EXISTS `schedule` (
    `schedule_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '排课主键ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `classroom_id`   BIGINT       NOT NULL                COMMENT '教室ID',
    `teacher_id`     BIGINT       NOT NULL                COMMENT '授课教师ID（user_id）',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期（如：2025-2026-1）',
    `week_day`       INT          NOT NULL                COMMENT '星期几：1-7',
    `start_period`   INT          NOT NULL                COMMENT '开始节次：1-12',
    `end_period`     INT          NOT NULL                COMMENT '结束节次：2-12',
    `start_week`     INT          DEFAULT 1               COMMENT '起始周',
    `end_week`       INT          DEFAULT 16              COMMENT '结束周',
    `schedule_type`  VARCHAR(16)  DEFAULT '正常'          COMMENT '类型：正常/调课/补课',
    PRIMARY KEY (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课课表表';
```

### 3. schedule_change（调课申请表）

```sql
CREATE TABLE IF NOT EXISTS `schedule_change` (
    `change_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '调课申请主键ID',
    `schedule_id`    BIGINT       NOT NULL                COMMENT '原排课ID',
    `apply_user_id`  BIGINT       NOT NULL                COMMENT '申请人ID',
    `reason`         VARCHAR(256) NOT NULL                COMMENT '调课原因',
    `new_week_day`   INT          DEFAULT NULL            COMMENT '新星期几',
    `new_start_period` INT        DEFAULT NULL            COMMENT '新开始节次',
    `new_end_period` INT          DEFAULT NULL            COMMENT '新结束节次',
    `new_classroom_id` BIGINT     DEFAULT NULL            COMMENT '新教室ID',
    `status`         INT          DEFAULT 0               COMMENT '状态：0=待审批, 1=已通过, 2=已拒绝',
    `approve_user_id` BIGINT      DEFAULT NULL            COMMENT '审批人ID',
    `approve_remark` VARCHAR(256) DEFAULT NULL            COMMENT '审批意见',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调课申请表';
```

### 4. course_selection（选课记录表）

```sql
CREATE TABLE IF NOT EXISTS `course_selection` (
    `selection_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '选课记录主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `schedule_id`    BIGINT       DEFAULT NULL            COMMENT '对应排课ID',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=已选, 2=退选, 3=补选',
    `select_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    PRIMARY KEY (`selection_id`),
    UNIQUE KEY `uk_student_course_semester` (`student_id`, `course_id`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';
```

### 5. course_capacity（课程容量配置表）

```sql
CREATE TABLE IF NOT EXISTS `course_capacity` (
    `capacity_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '容量配置主键ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `schedule_id`    BIGINT       DEFAULT NULL            COMMENT '对应排课ID',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `max_capacity`   INT          NOT NULL DEFAULT 60     COMMENT '最大选课人数',
    `current_count`  INT          NOT NULL DEFAULT 0      COMMENT '当前已选人数',
    `min_capacity`   INT          DEFAULT 10              COMMENT '开课最低人数',
    PRIMARY KEY (`capacity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程容量配置表';
```

### 6. exam（考试安排表）

```sql
CREATE TABLE IF NOT EXISTS `exam` (
    `exam_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '考试主键ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `exam_name`      VARCHAR(128) NOT NULL                COMMENT '考试名称（如：2025秋-高数期末）',
    `exam_type`      VARCHAR(16)  NOT NULL                COMMENT '类型：期末考试/补考/重修考试',
    `exam_date`      DATE         NOT NULL                COMMENT '考试日期',
    `start_time`     TIME         NOT NULL                COMMENT '开始时间',
    `end_time`       TIME         NOT NULL                COMMENT '结束时间',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    PRIMARY KEY (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';
```

### 7. exam_room（考试教室/座位表）

```sql
CREATE TABLE IF NOT EXISTS `exam_room` (
    `exam_room_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '考试教室主键ID',
    `exam_id`        BIGINT       NOT NULL                COMMENT '考试ID',
    `classroom_id`   BIGINT       NOT NULL                COMMENT '教室ID',
    `seat_no`        VARCHAR(16)  DEFAULT NULL            COMMENT '座位号',
    `student_id`     BIGINT       DEFAULT NULL            COMMENT '安排的学生ID',
    PRIMARY KEY (`exam_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试教室座位表';
```

### 8. invigilation（监考安排表）

```sql
CREATE TABLE IF NOT EXISTS `invigilation` (
    `invigilation_id` BIGINT      NOT NULL AUTO_INCREMENT COMMENT '监考主键ID',
    `exam_id`         BIGINT      NOT NULL                COMMENT '考试ID',
    `teacher_id`      BIGINT      NOT NULL                COMMENT '监考教师ID（user_id）',
    `classroom_id`    BIGINT      NOT NULL                COMMENT '监考教室ID',
    `duty`            VARCHAR(16) DEFAULT '主监考'        COMMENT '职责：主监考/副监考',
    PRIMARY KEY (`invigilation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监考安排表';
```

### 9. resit_apply（补考/重修报名表）

```sql
CREATE TABLE IF NOT EXISTS `resit_apply` (
    `apply_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报名主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `exam_id`        BIGINT       DEFAULT NULL            COMMENT '对应补考/重修考试ID',
    `apply_type`     VARCHAR(16)  NOT NULL                COMMENT '类型：补考/重修',
    `reason`         VARCHAR(256) DEFAULT NULL            COMMENT '申请原因',
    `status`         INT          DEFAULT 0               COMMENT '状态：0=待审核, 1=已通过, 2=已拒绝',
    `apply_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补考重修报名表';
```

### 10. graduation_topic（毕业设计课题表）

```sql
CREATE TABLE IF NOT EXISTS `graduation_topic` (
    `topic_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课题主键ID',
    `title`          VARCHAR(128) NOT NULL                COMMENT '课题标题',
    `description`    TEXT         DEFAULT NULL            COMMENT '课题描述与要求',
    `teacher_id`     BIGINT       NOT NULL                COMMENT '指导教师ID',
    `major_require`  VARCHAR(64)  DEFAULT NULL            COMMENT '专业要求',
    `max_student`    INT          DEFAULT 1               COMMENT '最多可选学生数',
    `current_student` INT         DEFAULT 0               COMMENT '已选学生数',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=可选, 0=已满, -1=已结束',
    PRIMARY KEY (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计课题表';
```

### 11. graduation_selection（毕业设计选题表）

```sql
CREATE TABLE IF NOT EXISTS `graduation_selection` (
    `select_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '选题主键ID',
    `topic_id`       BIGINT       NOT NULL                COMMENT '课题ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `status`         INT          DEFAULT 0               COMMENT '状态：0=待确认, 1=已确认, 2=已拒绝',
    `select_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '选题时间',
    PRIMARY KEY (`select_id`),
    UNIQUE KEY `uk_student_topic` (`student_id`, `topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计选题表';
```

### 12. graduation_report（毕业设计报告表）

```sql
CREATE TABLE IF NOT EXISTS `graduation_report` (
    `report_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报告主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `topic_id`       BIGINT       NOT NULL                COMMENT '课题ID',
    `report_type`    VARCHAR(16)  NOT NULL                COMMENT '类型：开题报告/中期报告/论文初稿/最终稿',
    `file_url`       VARCHAR(256) DEFAULT NULL            COMMENT '文件地址',
    `content`        TEXT         DEFAULT NULL            COMMENT '报告摘要',
    `submit_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `teacher_feedback` TEXT       DEFAULT NULL            COMMENT '教师批注/反馈',
    `feedback_time`  DATETIME     DEFAULT NULL            COMMENT '反馈时间',
    `score`          INT          DEFAULT NULL            COMMENT '评分',
    PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计报告表';
```

### 13. study_material（AI 学习资料表）

```sql
CREATE TABLE IF NOT EXISTS `study_material` (
    `material_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资料主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '上传学生ID',
    `course_id`      BIGINT       DEFAULT NULL            COMMENT '关联课程ID',
    `file_name`      VARCHAR(128) NOT NULL                COMMENT '文件名',
    `file_url`       VARCHAR(256) NOT NULL                COMMENT '文件地址',
    `file_type`      VARCHAR(16)  DEFAULT NULL            COMMENT '类型：课件/大纲/笔记',
    `upload_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI学习资料表';
```

### 14. ai_study_record（AI 学习记录表）

```sql
CREATE TABLE IF NOT EXISTS `ai_study_record` (
    `record_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `material_id`    BIGINT       DEFAULT NULL            COMMENT '关联资料ID',
    `record_type`    VARCHAR(32)  NOT NULL                COMMENT '类型：summary/question/path_recommend',
    `prompt`         TEXT         DEFAULT NULL            COMMENT '用户输入的提示词',
    `ai_response`    TEXT         DEFAULT NULL            COMMENT 'AI返回内容',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI学习记录表';
```

---

## A1：排课与课表管理系统

### 业务流程图

```
┌──────────┐    CRUD课程/教室    ┌──────────┐    查询课表    ┌──────────┐
│  教务人员  │ ────────────────→ │ 排课系统  │ ←──────────── │  学生    │
└──────────┘                    │          │               └──────────┘
       │                        │ 自动/手动 │                     ↑
       │  调课审批              │   排课   │                     │
       ↓                        │          │               ┌──────────┐
┌──────────┐   调课申请          │          │    查询课表     │  教师    │
│  教师    │ ─────────────────→  └──────────┘ ←───────────  └──────────┘
└──────────┘
```

### 后端实现

#### Entity

`Schedule.java`
```java
@Data @ToString @TableName(value = "schedule")
public class Schedule implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long scheduleId;
    private Long courseId;
    private Long classroomId;
    private Long teacherId;
    private String semester;
    private Integer weekDay;
    private Integer startPeriod;
    private Integer endPeriod;
    private Integer startWeek;
    private Integer endWeek;
    private String scheduleType;
}
```

#### Mapper

```java
public interface ScheduleMapper extends BaseMapper<Schedule> {
    // 根据教师ID和学期查询课表
    List<Schedule> selectByTeacher(@Param("teacherId") Long teacherId,
                                    @Param("semester") String semester);
    // 根据学生选课结果查询课表（关联 course_selection）
    List<Schedule> selectByStudent(@Param("studentId") Long studentId,
                                    @Param("semester") String semester);
    // 检查教室时间冲突
    int checkClassroomConflict(@Param("classroomId") Long classroomId,
                               @Param("weekDay") Integer weekDay,
                               @Param("startPeriod") Integer startPeriod,
                               @Param("endPeriod") Integer endPeriod,
                               @Param("semester") String semester);
}
```

> SQL 写在 `ScheduleMapper.xml` 中，放在 `campus-app/src/main/resources/mapper/` 下。

#### Service

```java
public interface IScheduleService extends IService<Schedule> {
    // 教务：新增排课（含冲突校验）
    CommonResult addSchedule(Schedule schedule);
    // 教务：删除排课
    CommonResult deleteSchedule(Long scheduleId);
    // 学生课表查询（日历格式，按星期几分组）
    JSONObject getStudentSchedule(Long studentId, String semester);
    // 教师课表查询
    JSONObject getTeacherSchedule(Long teacherId, String semester);
    // 调课申请提交
    CommonResult applyScheduleChange(ScheduleChange change);
    // 调课审批
    CommonResult approveScheduleChange(Long changeId, Integer status, String remark);
}
```

#### Controller

```java
@RestController @RequestMapping("/teaching/schedule") @Tag(name = "排课与课表管理")
public class ScheduleController {
    @Autowired IScheduleService scheduleService;

    @RequestMapping("/add") @Operation(summary = "新增排课")
    public CommonResult addSchedule(@RequestBody Schedule schedule) { ... }

    @RequestMapping("/delete") @Operation(summary = "删除排课")
    public CommonResult deleteSchedule(@RequestBody Schedule schedule) { ... }

    @RequestMapping("/student/{studentId}") @Operation(summary = "学生课表查询")
    public CommonResult getStudentSchedule(@PathVariable Long studentId,
            @RequestParam String semester) { ... }

    @RequestMapping("/teacher/{teacherId}") @Operation(summary = "教师课表查询")
    public CommonResult getTeacherSchedule(@PathVariable Long teacherId,
            @RequestParam String semester) { ... }

    @RequestMapping("/change/apply") @Operation(summary = "调课申请")
    public CommonResult applyChange(@RequestBody ScheduleChange change) { ... }

    @RequestMapping("/change/approve") @Operation(summary = "调课审批")
    public CommonResult approveChange(@RequestBody ScheduleChange change) { ... }
}
```

**核心逻辑——排课冲突校验**：
```java
// 在 ServiceImpl 中实现
private boolean hasConflict(Schedule schedule) {
    // 同一教室、同一学期、同一天、同一时段 → 冲突
    LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Schedule::getClassroomId, schedule.getClassroomId())
           .eq(Schedule::getSemester, schedule.getSemester())
           .eq(Schedule::getWeekDay, schedule.getWeekDay())
           .and(w -> w.between(Schedule::getStartPeriod, schedule.getStartPeriod(), schedule.getEndPeriod())
                      .or().between(Schedule::getEndPeriod, schedule.getStartPeriod(), schedule.getEndPeriod()));
    return this.count(wrapper) > 0;
}
```

### 前端实现

**路由**：`/home/course-schedule`

**页面组件**：
- `views/teaching/ScheduleView.vue` — 学生/教师课表主页面（日历式）
- `views/teaching/ScheduleManage.vue` — 教务排课管理页（表格 + 新增/编辑弹窗）
- `views/teaching/ScheduleChange.vue` — 调课申请与审批页

**课表日历展示**核心思路：使用 Element Plus `el-calendar` 或自定义 7 列网格（周一~周日），每个格子内渲染当日课程卡片。根据 `weekDay` 分组，`startPeriod` 排序。

```vue
<!-- 课表卡片示意 -->
<div class="schedule-grid">
    <div v-for="day in 7" :key="day" class="schedule-day">
        <div class="day-header">星期{{ day }}</div>
        <div v-for="item in scheduleByDay[day]" :key="item.scheduleId"
             class="schedule-card">
            <div>{{ item.courseName }}</div>
            <div>{{ item.classroomName }}</div>
            <div>第{{ item.startPeriod }}-{{ item.endPeriod }}节</div>
        </div>
    </div>
</div>
```

---

## A2：选课与容量控制系统

### 业务流程图

```
                    ┌─────────────────────────┐
                    │    选课与容量控制系统      │
                    └─────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ↓                    ↓                    ↓
   ┌──────────┐        ┌──────────┐         ┌──────────┐
   │  学生    │        │  教师    │         │  教务    │
   │ 在线选课  │        │ 查看名单  │         │ 调整容量  │
   │ 退选/补选 │        │          │         │          │
   └──────────┘        └──────────┘         └──────────┘
```

### 后端实现

**核心业务逻辑：**

```java
// 选课——库存校验 + 冲突校验
public CommonResult selectCourse(Long studentId, Long courseId, String semester) {
    // 1. 检查课程容量
    CourseCapacity capacity = capacityMapper.selectByCourseAndSemester(courseId, semester);
    if (capacity.getCurrentCount() >= capacity.getMaxCapacity()) {
        return CommonResult.error(903, "该课程已满，请选择其他课程");
    }
    // 2. 检查是否已选
    LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(CourseSelection::getStudentId, studentId)
           .eq(CourseSelection::getCourseId, courseId)
           .eq(CourseSelection::getSemester, semester);
    if (selectionMapper.selectCount(wrapper) > 0) {
        return CommonResult.error(904, "已选过该课程");
    }
    // 3. 检查时间冲突（查询学生已有课表，比对节次）
    List<Schedule> existing = scheduleMapper.selectByStudent(studentId, semester);
    Schedule newSchedule = scheduleMapper.selectByCourse(courseId, semester);
    for (Schedule s : existing) {
        if (timeOverlap(s, newSchedule)) {
            return CommonResult.error(905, "与已有课程时间冲突");
        }
    }
    // 4. 执行选课
    CourseSelection selection = new CourseSelection();
    selection.setStudentId(studentId);
    selection.setCourseId(courseId);
    selection.setSemester(semester);
    selectionMapper.insert(selection);
    // 5. 更新容量
    capacity.setCurrentCount(capacity.getCurrentCount() + 1);
    capacityMapper.updateById(capacity);
    return CommonResult.success();
}
```

**Controller：**

```java
@RestController @RequestMapping("/teaching/selection") @Tag(name = "选课与容量控制")
public class CourseSelectionController {
    @RequestMapping("/select") @Operation(summary = "学生选课")
    public CommonResult selectCourse(@RequestBody CourseSelection selection) { ... }

    @RequestMapping("/drop") @Operation(summary = "学生退选")
    public CommonResult dropCourse(@RequestBody CourseSelection selection) { ... }

    @RequestMapping("/my/{studentId}") @Operation(summary = "我的选课列表")
    public CommonResult getMySelection(@PathVariable Long studentId,
            @RequestParam String semester) { ... }

    @RequestMapping("/student-list/{courseId}") @Operation(summary = "教师查看选课名单")
    public CommonResult getStudentList(@PathVariable Long courseId) { ... }

    @RequestMapping("/capacity/update") @Operation(summary = "教务调整容量")
    public CommonResult updateCapacity(@RequestBody CourseCapacity capacity) { ... }
}
```

### 前端实现

**路由**：`/home/course-selection`

**页面组件**：
- `views/teaching/CourseSelection.vue` — 学生选课主页（课程列表 + 选课/退选按钮）
- `views/teaching/SelectionList.vue` — 教师查看选课名单
- `views/teaching/CapacityManage.vue` — 教务容量管理

**选课卡片** `el-card` 展示课程信息，每个卡片包含：课程名、教师、时间、教室、已选/容量，以及"选课"/"退选"按钮。

---

## A3：成绩评定与预警系统

### 业务流程图

```
教师录入成绩 → 系统计算绩点 → 标注不及格 → 汇总学分 → 学分不足? → 触发学业预警
```

### 成绩录入与计算

**绩点计算规则**（示例）：
| 分数 | 绩点 |
|------|------|
| 90-100 | 4.0 |
| 80-89 | 3.0 |
| 70-79 | 2.0 |
| 60-69 | 1.0 |
| <60 | 0.0（不及格） |

**学业预警规则**：当前学期不及格学分 > 设定阈值 → 触发预警通知。

```java
// 绩点计算
private double calculateGPA(int score) {
    if (score >= 90) return 4.0;
    else if (score >= 80) return 3.0;
    else if (score >= 70) return 2.0;
    else if (score >= 60) return 1.0;
    else return 0.0;
}

// 预警检查——在成绩提交后自动调用
public void checkAcademicWarning(Long studentId) {
    // 统计当前学期不及格学分
    LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Score::getStudentId, studentId)
           .lt(Score::getScoreScore, 60);
    long failCount = scoreMapper.selectCount(wrapper);
    if (failCount >= 3) { // 3门及以上不及格
        // 生成预警记录
        sendWarningNotification(studentId, "学业预警：您有" + failCount + "门课程不及格");
    }
}
```

**Controller：**

```java
@RestController @RequestMapping("/teaching/score") @Tag(name = "成绩评定与预警")
public class ScoreController {
    @RequestMapping("/input") @Operation(summary = "教师录入/修改成绩")
    public CommonResult inputScore(@RequestBody ScoreEntity score) { ... }

    @RequestMapping("/batch-input") @Operation(summary = "批量导入成绩")
    public CommonResult batchInputScore(@RequestBody List<ScoreEntity> scores) { ... }

    @RequestMapping("/my/{studentId}") @Operation(summary = "学生查询个人成绩")
    public CommonResult getMyScore(@PathVariable Long studentId,
            @RequestParam String semester) { ... }

    @RequestMapping("/gpa/{studentId}") @Operation(summary = "计算绩点")
    public CommonResult calculateGPA(@PathVariable Long studentId,
            @RequestParam String semester) { ... }

    @RequestMapping("/warning/{studentId}") @Operation(summary = "查询学业预警")
    public CommonResult getWarning(@PathVariable Long studentId) { ... }
}
```

### 前端实现

**路由**：`/home/score-management`

**页面组件**：
- `views/teaching/ScoreInput.vue` — 教师成绩录入（`el-table` 可编辑行 + 批量导入）
- `views/teaching/ScoreView.vue` — 学生成绩查询（`el-table` + 绩点汇总卡片）
- `views/teaching/WarningList.vue` — 预警学生列表

---

## A4：考试安排与重修补考系统

### 业务流程图

```
教务发布考试安排 → 教师查询监考任务
                 → 学生查询考试时间/座位
                 → 不及格学生在线报名补考/重修
```

### 后端实现

**Controller：**

```java
@RestController @RequestMapping("/teaching/exam") @Tag(name = "考试安排与重修补考")
public class ExamController {

    // ===== 考试安排 =====
    @RequestMapping("/publish") @Operation(summary = "教务发布考试安排")
    public CommonResult publishExam(@RequestBody ExamEntity exam) { ... }

    @RequestMapping("/room/assign") @Operation(summary = "安排考试座位")
    public CommonResult assignSeat(@RequestBody List<ExamRoom> rooms) { ... }

    @RequestMapping("/invigilation/assign") @Operation(summary = "安排监考")
    public CommonResult assignInvigilation(@RequestBody Invigilation inv) { ... }

    @RequestMapping("/my-exams/{studentId}") @Operation(summary = "学生查询考试安排")
    public CommonResult getStudentExams(@PathVariable Long studentId) { ... }

    @RequestMapping("/invigilation/{teacherId}") @Operation(summary = "教师查询监考任务")
    public CommonResult getInvigilationTasks(@PathVariable Long teacherId) { ... }

    // ===== 补考/重修 =====
    @RequestMapping("/resit/apply") @Operation(summary = "学生补考/重修报名")
    public CommonResult applyResit(@RequestBody ResitApply apply) { ... }

    @RequestMapping("/resit/approve") @Operation(summary = "教务审核补考报名")
    public CommonResult approveResit(@RequestBody ResitApply apply) { ... }

    @RequestMapping("/resit/list/{studentId}") @Operation(summary = "学生查询补考报名状态")
    public CommonResult getResitList(@PathVariable Long studentId) { ... }
}
```

**核心逻辑——考试座位分配**：
```java
public void assignSeats(Long examId, Long classroomId) {
    // 1. 查询该考试对应的选课学生列表
    List<Long> studentIds = selectionMapper.selectStudentIdsByCourse(examId);
    // 2. 查询教室容量
    Classroom room = classroomMapper.selectById(classroomId);
    // 3. 随机/按学号分配座位
    for (int i = 0; i < studentIds.size(); i++) {
        ExamRoom seat = new ExamRoom();
        seat.setExamId(examId);
        seat.setClassroomId(classroomId);
        seat.setStudentId(studentIds.get(i));
        seat.setSeatNo(String.valueOf(i + 1));
        examRoomMapper.insert(seat);
    }
}
```

### 前端实现

**路由**：`/home/exam-arrangement`

**页面组件**：
- `views/teaching/ExamPublish.vue` — 教务发布考试（`el-form` + 日期时间选择器）
- `views/teaching/ExamSeat.vue` — 学生考试座位查询
- `views/teaching/Invigilation.vue` — 教师监考任务查询
- `views/teaching/ResitApply.vue` — 补考/重修报名

---

## A5：毕业设计过程管理系统

### 业务流程图

```
教师发布课题 → 学生选题 → 教师确认 → 学生提交中期报告 → 教师批注/评分
```

### 后端实现

**Controller：**

```java
@RestController @RequestMapping("/teaching/graduation") @Tag(name = "毕业设计过程管理")
public class GraduationController {

    @RequestMapping("/topic/publish") @Operation(summary = "教师发布毕业设计课题")
    public CommonResult publishTopic(@RequestBody GraduationTopic topic) { ... }

    @RequestMapping("/topic/list") @Operation(summary = "学生查看可选课题列表")
    public CommonResult getTopicList(@RequestParam String semester) { ... }

    @RequestMapping("/topic/select") @Operation(summary = "学生选题")
    public CommonResult selectTopic(@RequestBody GraduationSelection selection) { ... }

    @RequestMapping("/topic/confirm") @Operation(summary = "教师确认选题")
    public CommonResult confirmSelection(@RequestBody GraduationSelection sel) { ... }

    @RequestMapping("/report/submit") @Operation(summary = "学生提交报告")
    public CommonResult submitReport(@RequestBody GraduationReport report) { ... }

    @RequestMapping("/report/feedback") @Operation(summary = "教师批注反馈")
    public CommonResult giveFeedback(@RequestBody GraduationReport report) { ... }

    @RequestMapping("/report/my/{studentId}") @Operation(summary = "学生查看报告提交历史")
    public CommonResult getMyReports(@PathVariable Long studentId) { ... }
}
```

**核心逻辑——选题校验**：
```java
public CommonResult selectTopic(Long studentId, Long topicId) {
    // 1. 检查课题是否可选
    GraduationTopic topic = topicMapper.selectById(topicId);
    if (topic.getStatus() != 1) {
        return CommonResult.error(906, "该课题已不可选");
    }
    if (topic.getCurrentStudent() >= topic.getMaxStudent()) {
        return CommonResult.error(907, "该课题已满");
    }
    // 2. 检查该学生是否已选过其他课题
    LambdaQueryWrapper<GraduationSelection> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(GraduationSelection::getStudentId, studentId)
           .eq(GraduationSelection::getStatus, 1);
    if (selectionMapper.selectCount(wrapper) > 0) {
        return CommonResult.error(908, "你已选过课题，请勿重复选择");
    }
    // 3. 执行选题
    GraduationSelection sel = new GraduationSelection();
    sel.setTopicId(topicId);
    sel.setStudentId(studentId);
    sel.setStatus(0); // 待确认
    selectionMapper.insert(sel);
    return CommonResult.success();
}
```

### 前端实现

**路由**：`/home/graduation-design`

**页面组件**：
- `views/teaching/GraduationTopic.vue` — 课题发布/浏览（`el-card` 列表）
- `views/teaching/GraduationSelect.vue` — 学生选题
- `views/teaching/GraduationReport.vue` — 报告提交（`el-upload` + `el-input` textarea）
- `views/teaching/GraduationFeedback.vue` — 教师批注（`el-timeline` 展示提交历史）

---

## A6：AI 智能学习助理 :bulb:

### 业务流程

```
学生上传课件/大纲 → AI解析文本 → 生成知识点摘要/复习题
学生历史成绩 + 选课记录 → AI分析 → 推荐个性化学习路径
```

### 后端实现

参照示例中的 `LanguageModelService`、`ChatModelService` 的代码风格。

**Service：**

```java
@Service
public class AIStudyService {

    @Autowired DashScopeChatModel dashScopeChatModel;

    /**
     * 上传课件，AI 生成知识点摘要
     */
    public String generateSummary(Long materialId) {
        StudyMaterial material = materialMapper.selectById(materialId);
        // 读取文件内容（简化处理——假设文本可解析）
        String fileContent = readFileContent(material.getFileUrl());

        String prompt = """
            请根据以下课件内容，生成一份结构化的知识点摘要：
            1. 列出3-5个核心知识点
            2. 每个知识点用2-3句话解释
            3. 标注重点与难点

            课件内容：
            %s
            """.formatted(fileContent);

        String result = dashScopeChatModel.call(prompt);

        // 保存AI记录
        AiStudyRecord record = new AiStudyRecord();
        record.setStudentId(material.getStudentId());
        record.setMaterialId(materialId);
        record.setRecordType("summary");
        record.setPrompt("课件摘要生成");
        record.setAiResponse(result);
        recordMapper.insert(record);

        return result;
    }

    /**
     * AI 生成复习题
     */
    public String generateQuestions(Long materialId) {
        StudyMaterial material = materialMapper.selectById(materialId);
        String fileContent = readFileContent(material.getFileUrl());

        String prompt = """
            根据以下课程内容，生成5道复习题（含选择题和简答题），并附带标准答案：
            内容：
            %s
            """.formatted(fileContent);

        return dashScopeChatModel.call(prompt);
    }

    /**
     * AI 自适应学习路径推荐
     */
    public String recommendStudyPath(Long studentId) {
        // 1. 查询学生历史成绩
        List<ScoreEntity> scores = scoreMapper.selectByStudent(studentId);
        // 2. 查询已选课程
        List<CourseSelection> selections = selectionMapper.selectByStudent(studentId);
        // 3. 组装 prompt
        String scoresJson = JSON.toJSONString(scores);
        String coursesJson = JSON.toJSONString(selections);

        String prompt = """
            你是一位学习规划专家。根据以下学生的历史成绩和选课情况，推荐个性化的学习路径：
            - 分析学生的优势和薄弱科目
            - 推荐下学期的选课建议
            - 给出每天的学习时间分配建议

            历史成绩：%s
            已选课程：%s
            """.formatted(scoresJson, coursesJson);

        return dashScopeChatModel.call(prompt);
    }
}
```

**Controller：**

```java
@RestController @RequestMapping("/teaching/ai") @Tag(name = "AI智能学习助理")
public class AIStudyController {

    @Autowired AIStudyService aiStudyService;

    @RequestMapping("/summary/{materialId}") @Operation(summary = "AI生成知识点摘要")
    public CommonResult generateSummary(@PathVariable Long materialId) {
        String result = aiStudyService.generateSummary(materialId);
        return CommonResult.success(result);
    }

    @RequestMapping("/questions/{materialId}") @Operation(summary = "AI生成复习题")
    public CommonResult generateQuestions(@PathVariable Long materialId) {
        String result = aiStudyService.generateQuestions(materialId);
        return CommonResult.success(result);
    }

    @RequestMapping("/path/{studentId}") @Operation(summary = "AI学习路径推荐")
    public CommonResult recommendPath(@PathVariable Long studentId) {
        String result = aiStudyService.recommendStudyPath(studentId);
        return CommonResult.success(result);
    }

    @RequestMapping("/material/upload") @Operation(summary = "上传学习资料")
    public CommonResult uploadMaterial(@RequestBody StudyMaterial material) { ... }

    @RequestMapping("/material/list/{studentId}") @Operation(summary = "我的学习资料列表")
    public CommonResult listMaterials(@PathVariable Long studentId) { ... }

    @RequestMapping("/record/list/{studentId}") @Operation(summary = "AI学习记录")
    public CommonResult listRecords(@PathVariable Long studentId) { ... }
}
```

> :bulb: `DashScopeChatModel` 已在 `campus-app/pom.xml` 依赖的 `spring-ai-alibaba-starter-dashscope` 中自动注入，`api-key` 配置在 `application.yml` 的 `spring.ai.dashscope.api-key`。Stream 流式返回使用 `Flux<String>` 配合前端 SSE。

### 前端实现

**路由**：`/home/ai-learning`

**页面组件**：
- `views/teaching/AIStudy.vue` — AI 学习助理主页

使用 `el-tabs` 切换三个功能区：

```
┌─────────────────────────────────────────────┐
│  [知识点摘要]  [复习题生成]  [学习路径推荐]    │
├─────────────────────────────────────────────┤
│  上传课件：[选择文件] [上传]                   │
│  已上传资料列表（el-table）                   │
│  [生成摘要] 按钮                              │
│  ┌─────────────────────────────┐             │
│  │  AI 生成的摘要内容（markdown） │             │
│  └─────────────────────────────┘             │
└─────────────────────────────────────────────┘
```

**API 定义**（在 `getData.js` 中添加）：

```js
// ========== AI 学习助理 API ==========
export const aiStudyAPI = {
    uploadMaterial(data) {
        return request({ url: '/teaching/ai/material/upload', method: 'post', data })
    },
    getMaterials(studentId) {
        return request({ url: `/teaching/ai/material/list/${studentId}`, method: 'get' })
    },
    generateSummary(materialId) {
        return request({ url: `/teaching/ai/summary/${materialId}`, method: 'post' })
    },
    generateQuestions(materialId) {
        return request({ url: `/teaching/ai/questions/${materialId}`, method: 'post' })
    },
    recommendPath(studentId) {
        return request({ url: `/teaching/ai/path/${studentId}`, method: 'post' })
    },
    getRecords(studentId) {
        return request({ url: `/teaching/ai/record/list/${studentId}`, method: 'get' })
    }
}
```

---

## 文件目录总览（成员 A 需创建的文件）

### 后端 Java 文件

```
backend/campus-app/src/main/java/com/smartcampus/app/
├── dao/teaching/
│   ├── ClassroomMapper.java
│   ├── ScheduleMapper.java
│   ├── ScheduleChangeMapper.java
│   ├── CourseSelectionMapper.java
│   ├── CourseCapacityMapper.java
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
│   ├── IClassroomService.java / impl/ClassroomServiceImpl.java
│   ├── IScheduleService.java / impl/ScheduleServiceImpl.java
│   ├── ICourseSelectionService.java / impl/CourseSelectionServiceImpl.java
│   ├── IExamService.java / impl/ExamServiceImpl.java
│   ├── IGraduationService.java / impl/GraduationServiceImpl.java
│   └── AIStudyService.java
└── controller/teaching/
    ├── ScheduleController.java
    ├── CourseSelectionController.java
    ├── ScoreController.java
    ├── ExamController.java
    ├── GraduationController.java
    └── AIStudyController.java

backend/campus-app/src/main/resources/mapper/
├── ScheduleMapper.xml
├── ExamMapper.xml
└── GraduationMapper.xml

backend/campus-contract/src/main/java/com/smartcampus/contract/entity/
├── Classroom.java
├── Schedule.java
├── ScheduleChange.java
├── CourseSelection.java
├── CourseCapacity.java
├── Exam.java
├── ExamRoom.java
├── Invigilation.java
├── ResitApply.java
├── GraduationTopic.java
├── GraduationSelection.java
├── GraduationReport.java
├── StudyMaterial.java
└── AiStudyRecord.java
```

### 前端文件

```
frontend/src/platform/src/
├── api/getData.js                          # 添加成员A的API函数
├── views/teaching/
│   ├── ScheduleView.vue                    # 课表日历
│   ├── ScheduleManage.vue                  # 排课管理
│   ├── ScheduleChange.vue                  # 调课申请审批
│   ├── CourseSelection.vue                 # 学生选课
│   ├── SelectionList.vue                   # 选课名单
│   ├── CapacityManage.vue                  # 容量管理
│   ├── ScoreInput.vue                      # 成绩录入
│   ├── ScoreView.vue                       # 成绩查询
│   ├── WarningList.vue                     # 预警列表
│   ├── ExamPublish.vue                     # 考试发布
│   ├── ExamSeat.vue                        # 考试座位
│   ├── Invigilation.vue                    # 监考任务
│   ├── ResitApply.vue                      # 补考报名
│   ├── GraduationTopic.vue                 # 课题管理
│   ├── GraduationSelect.vue                # 选题
│   ├── GraduationReport.vue                # 报告提交
│   ├── GraduationFeedback.vue              # 教师批注
│   └── AIStudy.vue                         # AI学习助理
```

### 数据库

```sql
-- 在 database/baseline/init.sql 末尾追加以上14张表的建表语句
```

---

## 实施顺序建议

| 优先级 | 模块 | 原因 |
|--------|------|------|
| 1 | A2 选课系统 | 依赖最小，先打通 CRUD + 容量校验 |
| 2 | A3 成绩系统 | 复用 student + course 表，逻辑独立 |
| 3 | A1 排课课表 | 依赖 classroom + course，数据准备后自然衔接 |
| 4 | A4 考试补考 | 依赖成绩数据（不及格判定） |
| 5 | A5 毕设管理 | 独立业务，最后做不影响其他模块 |
| 6 | A6 AI 助理 | AI 功能依赖 Spring AI 环境就绪后集中开发 |
