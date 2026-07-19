-- ============================================
-- 智慧校园服务平台 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS school_spring
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE school_spring;

-- ============================================
-- 共享基础表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    `username`   VARCHAR(64)  NOT NULL                COMMENT '用户名/账号',
    `password`   VARCHAR(100) NOT NULL                COMMENT 'BCrypt密码哈希',
    `user_type`  TINYINT      NOT NULL DEFAULT 1      COMMENT '人员类别: 1=学生, 2=教师, 3=教职工, 4=管理员',
    `real_name`  VARCHAR(32)  DEFAULT NULL            COMMENT '姓名',
    `gender`     TINYINT      DEFAULT NULL            COMMENT '性别: 1=男, 2=女',
    `phone`      VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    `email`      VARCHAR(64)  DEFAULT NULL            COMMENT '邮箱',
    `title`      VARCHAR(32)  DEFAULT NULL            COMMENT '职称',
    `position`   VARCHAR(32)  DEFAULT NULL            COMMENT '职务',
    `dept_id`    BIGINT       DEFAULT NULL            COMMENT '所属院系ID',
    `status`     TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1=启用, 0=停用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `role_id`   BIGINT      NOT NULL AUTO_INCREMENT,
    `role_code` VARCHAR(32) NOT NULL COMMENT '稳定角色编码',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `status`    TINYINT     NOT NULL DEFAULT 1,
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `permission_id`   BIGINT      NOT NULL AUTO_INCREMENT,
    `permission_code` VARCHAR(64) NOT NULL COMMENT 'domain:action权限码',
    `permission_name` VARCHAR(64) NOT NULL,
    `status`          TINYINT     NOT NULL DEFAULT 1,
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_user_role_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_role_permission_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 年级表
CREATE TABLE IF NOT EXISTS `grade` (
    `grade_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '年级主键ID',
    `grade_name` VARCHAR(32)  NOT NULL                COMMENT '年级名称',
    PRIMARY KEY (`grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';

-- 学生表
CREATE TABLE IF NOT EXISTS `student` (
    `student_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学生主键ID',
    `student_name`    VARCHAR(32)  NOT NULL                COMMENT '学生姓名',
    `student_birth`   VARCHAR(16)  DEFAULT NULL            COMMENT '学生生日',
    `student_address` VARCHAR(128) DEFAULT NULL            COMMENT '学生地址',
    `student_no`      BIGINT       NOT NULL                COMMENT '学号',
    `grade_id`        BIGINT       DEFAULT NULL            COMMENT '年级ID',
    `student_age`     INT          DEFAULT NULL            COMMENT '学生年龄',
    PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `course_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
    `course_name`      VARCHAR(64)  NOT NULL                COMMENT '课程名称',
    `course_code`      VARCHAR(16)  DEFAULT NULL            COMMENT '课程编号',
    `classification`   VARCHAR(8)   DEFAULT NULL            COMMENT '必修/选修/限选',
    `credit`           DECIMAL(3,1) DEFAULT NULL            COMMENT '学分 1-5',
    `weekly_frequency` INT          DEFAULT NULL            COMMENT '每周上课次数：1或2',
    `prerequisite_id`  BIGINT       DEFAULT NULL            COMMENT '先修课程ID，自关联',
    `is_active`        TINYINT      NOT NULL DEFAULT 1      COMMENT '是否开课: 1=正常, 0=停开',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `menu_id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
    `title`           VARCHAR(32)  NOT NULL                COMMENT '菜单标题',
    `path`            VARCHAR(64)  DEFAULT NULL            COMMENT '路由路径',
    `icon`            VARCHAR(32)  DEFAULT NULL            COMMENT '图标名称',
    `parent_id`       BIGINT       DEFAULT NULL            COMMENT '父菜单ID',
    `permission_code` VARCHAR(64)  DEFAULT NULL            COMMENT '可见权限码',
    `sort_order`      INT          NOT NULL DEFAULT 0,
    `status`          TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (`menu_id`),
    UNIQUE KEY `uk_menu_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 院系表
CREATE TABLE IF NOT EXISTS `department` (
    `dept_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '院系主键ID',
    `dept_name` VARCHAR(64)  NOT NULL                COMMENT '院系名称',
    `dept_code` VARCHAR(16)  DEFAULT NULL            COMMENT '院系编号',
    `description` TEXT       DEFAULT NULL            COMMENT '院系简介',
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- 专业表
CREATE TABLE IF NOT EXISTS `major` (
    `major_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专业主键ID',
    `dept_id`    BIGINT       NOT NULL                COMMENT '所属院系ID',
    `major_name` VARCHAR(64)  NOT NULL                COMMENT '专业名称',
    `major_code` VARCHAR(16)  DEFAULT NULL            COMMENT '专业编号',
    `cultivation_plan` TEXT   DEFAULT NULL            COMMENT '培养方案',
    PRIMARY KEY (`major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- ============================================
-- 成员 A：教务核心表
-- ============================================

-- 教室资源表
CREATE TABLE IF NOT EXISTS `classroom` (
    `classroom_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '教室主键ID',
    `classroom_name` VARCHAR(64)  NOT NULL                COMMENT '教室名称',
    `building`       VARCHAR(32)  DEFAULT NULL            COMMENT '所在教学楼',
    `capacity`       INT          NOT NULL DEFAULT 0      COMMENT '容纳人数',
    `type`           VARCHAR(16)  DEFAULT '普通教室'      COMMENT '类型：普通教室/多媒体/实验室/阶梯教室',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=可用, 0=维护中',
    PRIMARY KEY (`classroom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室资源表';

-- 排课课表表
CREATE TABLE IF NOT EXISTS `schedule` (
    `schedule_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '排课主键ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `classroom_id`   BIGINT       NOT NULL                COMMENT '教室ID',
    `teacher_id`     BIGINT       NOT NULL                COMMENT '授课教师ID',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `week_day`       INT          NOT NULL                COMMENT '星期几：1-7',
    `start_period`   INT          NOT NULL                COMMENT '开始节次',
    `end_period`     INT          NOT NULL                COMMENT '结束节次',
    `start_week`     INT          DEFAULT 1               COMMENT '起始周',
    `end_week`       INT          DEFAULT 16              COMMENT '结束周',
    `schedule_type`  VARCHAR(16)  DEFAULT '正常'          COMMENT '类型：正常/调课/补课',
    `parent_id`      BIGINT       DEFAULT NULL            COMMENT '跨时段分拆时指向首段排课ID',
    `week_pattern`   VARCHAR(16)  NOT NULL DEFAULT 'every' COMMENT '周模式: every/odd/even',
    `target_grade_id` BIGINT      DEFAULT NULL            COMMENT '选修课面向年级ID',
    PRIMARY KEY (`schedule_id`),
    KEY `idx_schedule_parent` (`parent_id`),
    KEY `idx_schedule_target_grade` (`target_grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课课表表';

-- 调课申请表
CREATE TABLE IF NOT EXISTS `schedule_change` (
    `change_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '调课申请主键ID',
    `schedule_id`      BIGINT       NOT NULL                COMMENT '原排课ID',
    `apply_user_id`    BIGINT       NOT NULL                COMMENT '申请人ID',
    `reason`           VARCHAR(256) NOT NULL                COMMENT '调课原因',
    `new_week_day`     INT          DEFAULT NULL            COMMENT '新星期几',
    `new_start_period` INT          DEFAULT NULL            COMMENT '新开始节次',
    `new_end_period`   INT          DEFAULT NULL            COMMENT '新结束节次',
    `new_classroom_id` BIGINT       DEFAULT NULL            COMMENT '新教室ID',
    `status`           INT          DEFAULT 0               COMMENT '状态：0=待审批, 1=已通过, 2=已拒绝',
    `approve_user_id`  BIGINT       DEFAULT NULL            COMMENT '审批人ID',
    `approve_remark`   VARCHAR(256) DEFAULT NULL            COMMENT '审批意见',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调课申请表';

-- 选课记录表
CREATE TABLE IF NOT EXISTS `course_selection` (
    `selection_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '选课记录主键ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `schedule_id`    BIGINT       DEFAULT NULL            COMMENT '对应排课ID',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=已选, 2=退选',
    `select_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    UNIQUE KEY `uk_student_course_sem` (`student_id`, `course_id`, `semester`),
    PRIMARY KEY (`selection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

-- 课程容量配置表
CREATE TABLE IF NOT EXISTS `course_capacity` (
    `capacity_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '容量主键ID',
    `course_id`      BIGINT       NOT NULL                COMMENT '课程ID',
    `semester`       VARCHAR(32)  NOT NULL                COMMENT '学期',
    `max_capacity`   INT          NOT NULL DEFAULT 60     COMMENT '最大选课人数',
    `current_count`  INT          NOT NULL DEFAULT 0      COMMENT '当前已选人数',
    `min_capacity`   INT          DEFAULT 10              COMMENT '开课最低人数',
    `schedule_id`    BIGINT       DEFAULT NULL            COMMENT '对应教学班排课ID',
    PRIMARY KEY (`capacity_id`),
    KEY `idx_course_capacity_schedule` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程容量配置表';

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `score_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '成绩主键ID',
    `student_id`  BIGINT NOT NULL                COMMENT '学生ID',
    `course_id`   BIGINT NOT NULL                COMMENT '课程ID',
    `score_score` INT    DEFAULT NULL            COMMENT '成绩分数',
    `semester`    VARCHAR(32) DEFAULT NULL       COMMENT '学期',
    `gpa`         DECIMAL(3,1) DEFAULT NULL      COMMENT '绩点',
    `status`      INT    DEFAULT 1               COMMENT '状态：1=正常, 0=不及格',
    PRIMARY KEY (`score_id`),
    UNIQUE KEY `uk_score_student_course_sem` (`student_id`, `course_id`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 考试安排表
CREATE TABLE IF NOT EXISTS `exam` (
    `exam_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '考试主键ID',
    `course_id`  BIGINT       NOT NULL                COMMENT '课程ID',
    `exam_name`  VARCHAR(128) NOT NULL                COMMENT '考试名称',
    `exam_type`  VARCHAR(16)  NOT NULL                COMMENT '类型：期末考试/补考/重修考试',
    `exam_date`  DATE         NOT NULL                COMMENT '考试日期',
    `start_time` TIME         NOT NULL                COMMENT '开始时间',
    `end_time`   TIME         NOT NULL                COMMENT '结束时间',
    `semester`   VARCHAR(32)  NOT NULL                COMMENT '学期',
    PRIMARY KEY (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

-- 考试座位表
CREATE TABLE IF NOT EXISTS `exam_room` (
    `exam_room_id` BIGINT      NOT NULL AUTO_INCREMENT COMMENT '考试座位主键ID',
    `exam_id`      BIGINT      NOT NULL                COMMENT '考试ID',
    `classroom_id` BIGINT      NOT NULL                COMMENT '教室ID',
    `seat_no`      VARCHAR(16) DEFAULT NULL            COMMENT '座位号',
    `student_id`   BIGINT      DEFAULT NULL            COMMENT '安排的学生ID',
    PRIMARY KEY (`exam_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试座位表';

-- 监考安排表
CREATE TABLE IF NOT EXISTS `invigilation` (
    `invigilation_id` BIGINT      NOT NULL AUTO_INCREMENT COMMENT '监考主键ID',
    `exam_id`         BIGINT      NOT NULL                COMMENT '考试ID',
    `teacher_id`      BIGINT      NOT NULL                COMMENT '监考教师ID',
    `classroom_id`    BIGINT      NOT NULL                COMMENT '监考教室ID',
    `duty`            VARCHAR(16) DEFAULT '主监考'        COMMENT '职责：主监考/副监考',
    PRIMARY KEY (`invigilation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监考安排表';

-- 补考重修报名表
CREATE TABLE IF NOT EXISTS `resit_apply` (
    `apply_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报名主键ID',
    `student_id` BIGINT       NOT NULL                COMMENT '学生ID',
    `course_id`  BIGINT       NOT NULL                COMMENT '课程ID',
    `exam_id`    BIGINT       DEFAULT NULL            COMMENT '对应考试ID',
    `apply_type` VARCHAR(16)  NOT NULL                COMMENT '类型：补考/重修',
    `reason`     VARCHAR(256) DEFAULT NULL            COMMENT '申请原因',
    `status`     INT          DEFAULT 0               COMMENT '状态：0=待审核, 1=已通过, 2=已拒绝',
    `apply_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补考重修报名表';

-- 毕业设计课题表
CREATE TABLE IF NOT EXISTS `graduation_topic` (
    `topic_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课题主键ID',
    `title`           VARCHAR(128) NOT NULL                COMMENT '课题标题',
    `description`     TEXT         DEFAULT NULL            COMMENT '课题描述与要求',
    `teacher_id`      BIGINT       NOT NULL                COMMENT '指导教师ID',
    `major_require`   VARCHAR(64)  DEFAULT NULL            COMMENT '专业要求',
    `max_student`     INT          DEFAULT 1               COMMENT '最多可选学生数',
    `current_student` INT          DEFAULT 0               COMMENT '已选学生数',
    `semester`        VARCHAR(32)  NOT NULL                COMMENT '学期',
    `status`          INT          DEFAULT 1               COMMENT '状态：1=可选, 0=已满',
    PRIMARY KEY (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计课题表';

-- 毕业设计选题表
CREATE TABLE IF NOT EXISTS `graduation_selection` (
    `select_id`   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '选题主键ID',
    `topic_id`    BIGINT   NOT NULL                COMMENT '课题ID',
    `student_id`  BIGINT   NOT NULL                COMMENT '学生ID',
    `status`      INT      DEFAULT 0               COMMENT '状态：0=待确认, 1=已确认, 2=已拒绝',
    `select_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选题时间',
    PRIMARY KEY (`select_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计选题表';

-- 毕业设计报告表
CREATE TABLE IF NOT EXISTS `graduation_report` (
    `report_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报告主键ID',
    `student_id`       BIGINT       NOT NULL                COMMENT '学生ID',
    `topic_id`         BIGINT       NOT NULL                COMMENT '课题ID',
    `report_type`      VARCHAR(16)  NOT NULL                COMMENT '类型：开题报告/中期报告/论文初稿/最终稿',
    `file_url`         VARCHAR(256) DEFAULT NULL            COMMENT '文件地址',
    `content`          TEXT         DEFAULT NULL            COMMENT '报告摘要',
    `submit_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `teacher_feedback` TEXT         DEFAULT NULL            COMMENT '教师批注/反馈',
    `feedback_time`    DATETIME     DEFAULT NULL            COMMENT '反馈时间',
    `score`            INT          DEFAULT NULL            COMMENT '评分',
    PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计报告表';

-- ============================================
-- 成员 B：学工事务表
-- ============================================

-- 学籍异动申请表
CREATE TABLE IF NOT EXISTS `student_status_change` (
    `change_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '异动主键ID',
    `student_id`    BIGINT       NOT NULL                COMMENT '学生ID',
    `change_type`   VARCHAR(16)  NOT NULL                COMMENT '类型：休学/复学/转专业/退学',
    `reason`        VARCHAR(512) NOT NULL                COMMENT '申请原因',
    `new_major_id`  BIGINT       DEFAULT NULL            COMMENT '转专业-新专业ID',
    `status`        INT          DEFAULT 0               COMMENT '状态：0=辅导员审核中, 1=教务审核中, 2=已通过, 3=已拒绝',
    `counselor_id`  BIGINT       DEFAULT NULL            COMMENT '辅导员ID',
    `counselor_opinion` VARCHAR(256) DEFAULT NULL        COMMENT '辅导员意见',
    `admin_id`      BIGINT       DEFAULT NULL            COMMENT '教务管理员ID',
    `admin_opinion` VARCHAR(256) DEFAULT NULL            COMMENT '教务意见',
    `apply_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学籍异动申请表';

-- 奖助贷申请表
CREATE TABLE IF NOT EXISTS `scholarship` (
    `scholarship_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请主键ID',
    `student_id`       BIGINT       NOT NULL                COMMENT '学生ID',
    `scholarship_type` VARCHAR(16)  NOT NULL                COMMENT '类型：奖学金/困难补助/助学贷款',
    `title`            VARCHAR(128) NOT NULL                COMMENT '申请标题',
    `reason`           TEXT         NOT NULL                COMMENT '申请理由',
    `attachment_url`   VARCHAR(256) DEFAULT NULL            COMMENT '附件地址',
    `status`           INT          DEFAULT 0               COMMENT '状态：0=待审核, 1=已通过, 2=已拒绝',
    `reviewer_id`      BIGINT       DEFAULT NULL            COMMENT '审核人ID',
    `review_opinion`   VARCHAR(256) DEFAULT NULL            COMMENT '审核意见',
    `apply_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`scholarship_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖助贷申请表';

-- 评教表
CREATE TABLE IF NOT EXISTS `evaluation` (
    `evaluation_id` BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评教主键ID',
    `student_id`    BIGINT   NOT NULL                COMMENT '学生ID',
    `schedule_id`   BIGINT   DEFAULT NULL            COMMENT '关联排课班次ID',
    `teacher_id`    BIGINT   NOT NULL                COMMENT '被评教师ID',
    `course_id`     BIGINT   NOT NULL                COMMENT '课程ID',
    `semester`      VARCHAR(32) NOT NULL             COMMENT '学期',
    `score_teaching` INT     DEFAULT NULL            COMMENT '教学态度评分 1-5',
    `score_content` INT      DEFAULT NULL            COMMENT '教学内容评分 1-5',
    `score_method`  INT      DEFAULT NULL            COMMENT '教学方法评分 1-5',
    `comment`       TEXT     DEFAULT NULL            COMMENT '匿名评价',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`evaluation_id`),
    UNIQUE KEY `uk_evaluation_student_schedule` (`student_id`, `schedule_id`),
    KEY `idx_evaluation_teacher_semester` (`teacher_id`, `semester`),
    KEY `idx_evaluation_teacher_course` (`teacher_id`, `course_id`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教表';

-- 竞赛信息表
CREATE TABLE IF NOT EXISTS `competition` (
    `competition_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '竞赛主键ID',
    `title`          VARCHAR(128) NOT NULL                COMMENT '竞赛标题',
    `description`    TEXT         DEFAULT NULL            COMMENT '竞赛描述',
    `publisher_id`   BIGINT       NOT NULL                COMMENT '发布教师ID',
    `deadline`       DATE         DEFAULT NULL            COMMENT '报名截止日期',
    `max_team_count` INT          DEFAULT 10              COMMENT '最大队伍数',
    `status`         INT          DEFAULT 1               COMMENT '状态：1=报名中, 2=进行中, 3=已结束',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛信息表';

-- 竞赛队伍表
CREATE TABLE IF NOT EXISTS `competition_team` (
    `team_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '队伍主键ID',
    `competition_id` BIGINT       NOT NULL                COMMENT '竞赛ID',
    `team_name`      VARCHAR(64)  NOT NULL                COMMENT '队伍名称',
    `leader_id`      BIGINT       NOT NULL                COMMENT '队长ID（学生）',
    `status`         INT          DEFAULT 0               COMMENT '状态：0=待审核, 1=已通过, 2=已拒绝',
    `reviewer_id`    BIGINT       DEFAULT NULL            COMMENT '审核教师ID',
    `apply_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛队伍表';

-- 竞赛队伍成员表
CREATE TABLE IF NOT EXISTS `competition_member` (
    `member_id`  BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员主键ID',
    `team_id`    BIGINT NOT NULL                COMMENT '队伍ID',
    `student_id` BIGINT NOT NULL                COMMENT '学生ID',
    `role`       VARCHAR(16) DEFAULT '队员'     COMMENT '角色：队长/队员',
    PRIMARY KEY (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛队伍成员表';

-- 实验室表
CREATE TABLE IF NOT EXISTS `lab` (
    `lab_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '实验室主键ID',
    `lab_name`    VARCHAR(64)  NOT NULL                COMMENT '实验室名称',
    `location`    VARCHAR(128) DEFAULT NULL            COMMENT '位置',
    `capacity`    INT          DEFAULT 0               COMMENT '容纳人数',
    `description` TEXT         DEFAULT NULL            COMMENT '描述',
    `status`      INT          DEFAULT 1               COMMENT '状态：1=开放, 0=维护中',
    PRIMARY KEY (`lab_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室表';

-- 实验室预约表
CREATE TABLE IF NOT EXISTS `lab_booking` (
    `booking_id`   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '预约主键ID',
    `lab_id`       BIGINT   NOT NULL                COMMENT '实验室ID',
    `student_id`   BIGINT   NOT NULL                COMMENT '学生ID',
    `booking_date` DATE     NOT NULL                COMMENT '预约日期',
    `start_period` INT      NOT NULL                COMMENT '开始节次',
    `end_period`   INT      NOT NULL                COMMENT '结束节次',
    `purpose`      VARCHAR(256) DEFAULT NULL        COMMENT '用途说明',
    `status`       INT      DEFAULT 1               COMMENT '状态：1=已预约, 2=已取消, 3=已完成',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
    PRIMARY KEY (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约表';

-- 心理问卷表
CREATE TABLE IF NOT EXISTS `psychology_questionnaire` (
    `questionnaire_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '问卷主键ID',
    `student_id`       BIGINT       NOT NULL                COMMENT '学生ID',
    `answers`          TEXT         DEFAULT NULL            COMMENT '问卷答案JSON',
    `risk_level`       VARCHAR(8)   DEFAULT NULL            COMMENT '风险等级：红/橙/黄/绿',
    `ai_analysis`      TEXT         DEFAULT NULL            COMMENT 'AI分析结果',
    `submit_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`questionnaire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理问卷表';

-- 心理预警记录表
CREATE TABLE IF NOT EXISTS `psychology_warning` (
    `warning_id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '预警主键ID',
    `student_id`       BIGINT      NOT NULL                COMMENT '学生ID',
    `warning_level`    VARCHAR(8)  NOT NULL                COMMENT '预警等级：红/橙/黄',
    `reason`           TEXT        DEFAULT NULL            COMMENT '预警原因',
    `counselor_id`     BIGINT      DEFAULT NULL            COMMENT '接收预警的辅导员ID',
    `is_read`          INT         DEFAULT 0               COMMENT '是否已读：0=未读, 1=已读',
    `handle_remark`    TEXT        DEFAULT NULL            COMMENT '处理备注',
    `create_time`      DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
    PRIMARY KEY (`warning_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理预警记录表';

-- ============================================
-- 成员 C：协同办公表
-- ============================================

-- 费用账单表
CREATE TABLE IF NOT EXISTS `fee` (
    `fee_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账单主键ID',
    `student_id`  BIGINT       NOT NULL                COMMENT '学生ID',
    `fee_type`    VARCHAR(16)  NOT NULL                COMMENT '类型：学费/报考费/住宿费',
    `amount`      DECIMAL(10,2) NOT NULL               COMMENT '金额',
    `semester`    VARCHAR(32)  DEFAULT NULL            COMMENT '学期',
    `status`      INT          DEFAULT 0               COMMENT '状态：0=未支付, 1=已支付',
    `due_date`    DATE         DEFAULT NULL            COMMENT '截止日期',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`fee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用账单表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment` (
    `payment_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '支付主键ID',
    `student_id`   BIGINT       NOT NULL                COMMENT '学生ID',
    `fee_id`       BIGINT       DEFAULT NULL            COMMENT '账单ID',
    `work_plan_id` BIGINT       DEFAULT NULL            COMMENT '勤工俭学任务ID',
    `amount`       DECIMAL(10,2) NOT NULL               COMMENT '支付金额',
    `payment_type` VARCHAR(16)  DEFAULT '学费'          COMMENT '类型：学费/一卡通充值/勤工俭学工资/消费',
    `description`  VARCHAR(128) DEFAULT NULL            COMMENT '描述',
    `payment_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `uk_payment_work_plan` (`work_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 固定资产表
CREATE TABLE IF NOT EXISTS `asset` (
    `asset_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资产主键ID',
    `asset_name`   VARCHAR(64)  NOT NULL                COMMENT '资产名称',
    `asset_type`   VARCHAR(16)  NOT NULL                COMMENT '类型：设备/办公用品/其他',
    `quantity`     INT          DEFAULT 1               COMMENT '数量',
    `dept_id`      BIGINT       NOT NULL DEFAULT 1      COMMENT '唯一部门ID（固定为1）',
    `user_id`      BIGINT       DEFAULT NULL            COMMENT '借用人ID',
    `status`       INT          DEFAULT 1               COMMENT '状态：1=在库, 2=已借出, 3=报废',
    `apply_user_id` BIGINT      DEFAULT NULL            COMMENT '申请人ID',
    `application_type` VARCHAR(16) DEFAULT NULL          COMMENT '申请类型：PURCHASE/ADD/BORROW/SCRAP/LEGACY；台账为空',
    `source_asset_id` BIGINT     DEFAULT NULL            COMMENT '借用或报废申请对应的来源资产ID',
    `application_reason` VARCHAR(512) DEFAULT NULL       COMMENT '申请原因或损坏情况',
    `approve_status` INT        DEFAULT 0               COMMENT '审批：0=待审批, 1=已通过, 2=已拒绝',
    `approve_user_id` BIGINT     DEFAULT NULL            COMMENT '管理员审批人ID',
    `approve_remark` VARCHAR(256) DEFAULT NULL           COMMENT '管理员审批意见',
    `approve_time`  DATETIME     DEFAULT NULL            COMMENT '管理员审批时间',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`asset_id`),
    KEY `idx_asset_application` (`application_type`, `approve_status`, `create_time`),
    KEY `idx_asset_source` (`source_asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定资产表';

-- 工作计划表
CREATE TABLE IF NOT EXISTS `work_plan` (
    `plan_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '计划主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '计划人或勤工俭学学生ID',
    `plan_type`   VARCHAR(16)  NOT NULL                COMMENT '类型：周计划/月计划/勤工俭学；指派任务为历史兼容值',
    `content`     TEXT         NOT NULL                COMMENT '计划内容',
    `start_date`  DATE         DEFAULT NULL            COMMENT '开始日期',
    `end_date`    DATE         DEFAULT NULL            COMMENT '结束日期',
    `status`      INT          DEFAULT 1               COMMENT '普通计划：1=进行中,2=已完成；勤工俭学：1=进行中,2=待确认,3=已结算',
    `supervisor_comment` TEXT  DEFAULT NULL            COMMENT '上级点评',
    `assigner_id` BIGINT       DEFAULT NULL            COMMENT '勤工俭学原指派人ID',
    `wage_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00  COMMENT '任务工资',
    `wage_paid`   TINYINT      NOT NULL DEFAULT 0      COMMENT '工资是否已发放：0=否,1=是',
    `wage_paid_time` DATETIME  DEFAULT NULL            COMMENT '工资发放时间',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`plan_id`),
    KEY `idx_work_plan_assigner` (`assigner_id`, `status`, `wage_paid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作计划表';

-- 公文表
CREATE TABLE IF NOT EXISTS `document` (
    `doc_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公文主键ID',
    `title`       VARCHAR(128) NOT NULL                COMMENT '公文标题',
    `doc_type`    VARCHAR(16)  NOT NULL                COMMENT '类型：公文会签/请示报告/请假申请',
    `content`     TEXT         NOT NULL                COMMENT '公文内容',
    `initiator_id` BIGINT      NOT NULL                COMMENT '发起人ID',
    `current_approver_id` BIGINT DEFAULT NULL          COMMENT '当前审批人ID',
    `status`      INT          DEFAULT 0               COMMENT '状态：0=审批中, 1=已通过, 2=已拒绝, 3=已退回',
    `approval_chain` TEXT      DEFAULT NULL            COMMENT '启动时生成的有序审批人链JSON快照',
    `workflow_id` BIGINT       DEFAULT NULL            COMMENT '启动时采用的流程版本ID',
    `current_step` INT         DEFAULT NULL            COMMENT '当前审批步骤',
    `approval_round` INT       NOT NULL DEFAULT 1      COMMENT '审批轮次',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文表';

-- 公文审批记录表
CREATE TABLE IF NOT EXISTS `document_approval` (
    `approval_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审批记录主键ID',
    `doc_id`      BIGINT       NOT NULL                COMMENT '公文ID',
    `task_id`     BIGINT       DEFAULT NULL            COMMENT '对应审批任务ID',
    `approver_id` BIGINT       NOT NULL                COMMENT '审批人ID',
    `round_no`    INT          DEFAULT NULL            COMMENT '审批轮次',
    `step_order`  INT          DEFAULT NULL            COMMENT '审批步骤',
    `step_name`   VARCHAR(64)  DEFAULT NULL            COMMENT '步骤名称快照',
    `action`      VARCHAR(8)   NOT NULL                COMMENT '操作：同意/拒绝/退回',
    `opinion`     VARCHAR(256) DEFAULT NULL            COMMENT '审批意见',
    `approval_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批记录表';

-- 公文审批资格配置表
CREATE TABLE IF NOT EXISTS `document_approver` (
    `approver_config_id` BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       NOT NULL,
    `display_name`       VARCHAR(32)  NOT NULL,
    `status`             TINYINT      NOT NULL DEFAULT 1,
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by`         BIGINT       DEFAULT NULL,
    `updated_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`approver_config_id`),
    UNIQUE KEY `uk_document_approver_user` (`user_id`),
    CONSTRAINT `fk_document_approver_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批资格配置表';

-- 公文审批流程版本表
CREATE TABLE IF NOT EXISTS `document_workflow` (
    `workflow_id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `workflow_name` VARCHAR(64)  NOT NULL,
    `doc_type`      VARCHAR(16)  NOT NULL,
    `version`       INT          NOT NULL,
    `status`        TINYINT      NOT NULL DEFAULT 1,
    `created_by`    BIGINT       NOT NULL,
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`workflow_id`),
    UNIQUE KEY `uk_document_workflow_type_version` (`doc_type`, `version`),
    KEY `idx_document_workflow_active` (`doc_type`, `status`),
    CONSTRAINT `fk_document_workflow_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程版本表';

-- 公文审批流程步骤表
CREATE TABLE IF NOT EXISTS `document_workflow_step` (
    `step_id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `workflow_id` BIGINT       NOT NULL,
    `step_order`  INT          NOT NULL,
    `step_name`   VARCHAR(64)  NOT NULL,
    `approver_id` BIGINT       NOT NULL,
    PRIMARY KEY (`step_id`),
    UNIQUE KEY `uk_document_workflow_step_order` (`workflow_id`, `step_order`),
    KEY `idx_document_workflow_step_approver` (`approver_id`),
    CONSTRAINT `fk_document_workflow_step_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_workflow_step_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程步骤表';

-- 公文逐级审批任务快照表
CREATE TABLE IF NOT EXISTS `document_approval_task` (
    `task_id`      BIGINT       NOT NULL AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `workflow_id`  BIGINT       DEFAULT NULL,
    `round_no`     INT          NOT NULL DEFAULT 1,
    `step_order`   INT          NOT NULL,
    `step_name`    VARCHAR(64)  NOT NULL,
    `approver_id`  BIGINT       NOT NULL,
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0=等待,1=待审批,2=同意,3=拒绝,4=退回,5=取消',
    `handled_time` DATETIME     DEFAULT NULL,
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_document_task_round_step` (`doc_id`, `round_no`, `step_order`),
    KEY `idx_document_task_pending` (`approver_id`, `status`),
    CONSTRAINT `fk_document_task_document` FOREIGN KEY (`doc_id`) REFERENCES `document` (`doc_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_task_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`),
    CONSTRAINT `fk_document_task_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文逐级审批任务快照表';

-- 会议表
CREATE TABLE IF NOT EXISTS `meeting` (
    `meeting_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会议主键ID',
    `title`        VARCHAR(128) NOT NULL                COMMENT '会议标题',
    `content`      TEXT         DEFAULT NULL            COMMENT '会议内容',
    `initiator_id` BIGINT       NOT NULL                COMMENT '发起人ID',
    `meeting_date` DATE         NOT NULL                COMMENT '会议日期',
    `start_time`   TIME         NOT NULL                COMMENT '开始时间',
    `end_time`     TIME         NOT NULL                COMMENT '结束时间',
    `location`     VARCHAR(128) DEFAULT NULL            COMMENT '会议地点',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议表';

-- 会议参会人员表
CREATE TABLE IF NOT EXISTS `meeting_attendee` (
    `attendee_id` BIGINT      NOT NULL AUTO_INCREMENT COMMENT '参会主键ID',
    `meeting_id`  BIGINT      NOT NULL                COMMENT '会议ID',
    `user_id`     BIGINT      NOT NULL                COMMENT '参会人ID',
    `status`      VARCHAR(8)  DEFAULT '待确认'        COMMENT '状态：待确认/参会/请假',
    `reply_time`  DATETIME    DEFAULT NULL            COMMENT '回复时间',
    PRIMARY KEY (`attendee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议参会人员表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `notify_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '接收人ID',
    `title`       VARCHAR(128) NOT NULL                COMMENT '通知标题',
    `content`     TEXT         DEFAULT NULL            COMMENT '通知内容',
    `notify_type` VARCHAR(16)  DEFAULT '系统通知'      COMMENT '类型：会议通知/系统通知/预警通知',
    `is_read`     INT          DEFAULT 0               COMMENT '是否已读：0=未读, 1=已读',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`notify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ============================================
-- 成员 D：基础数据与统计表
-- ============================================

-- 招生计划表
CREATE TABLE IF NOT EXISTS `enrollment` (
    `enrollment_id`  BIGINT NOT NULL AUTO_INCREMENT COMMENT '招生主键ID',
    `major_id`       BIGINT NOT NULL                COMMENT '专业ID',
    `plan_count`     INT    DEFAULT 0               COMMENT '计划招生人数',
    `actual_count`   INT    DEFAULT 0               COMMENT '实际报到人数',
    `year`           INT    NOT NULL                COMMENT '年度',
    `report_rate`    DECIMAL(5,2) DEFAULT NULL      COMMENT '报到率',
    PRIMARY KEY (`enrollment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招生计划表';

-- 新闻公告表
CREATE TABLE IF NOT EXISTS `news` (
    `news_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '新闻主键ID',
    `title`      VARCHAR(128) NOT NULL                COMMENT '标题',
    `content`    TEXT         DEFAULT NULL            COMMENT '内容',
    `news_type`  VARCHAR(16)  DEFAULT '公告'          COMMENT '类型：公告/新闻',
    `publisher_id` BIGINT     NOT NULL                COMMENT '发布人ID',
    `is_pinned`  INT          DEFAULT 0               COMMENT '是否置顶',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`news_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻公告表';

-- 论坛帖子表
CREATE TABLE IF NOT EXISTS `forum_post` (
    `post_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '帖子主键ID',
    `title`      VARCHAR(128) NOT NULL                COMMENT '帖子标题',
    `content`    TEXT         NOT NULL                COMMENT '帖子内容',
    `author_id`  BIGINT       NOT NULL                COMMENT '作者ID',
    `like_count` INT          DEFAULT 0               COMMENT '点赞数',
    `view_count` INT          DEFAULT 0               COMMENT '浏览数',
    `status`     INT          DEFAULT 1               COMMENT '状态：1=正常, 0=已删除, -1=已封禁',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发帖时间',
    PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- 论坛回复表
CREATE TABLE IF NOT EXISTS `forum_comment` (
    `comment_id` BIGINT   NOT NULL AUTO_INCREMENT COMMENT '回复主键ID',
    `post_id`    BIGINT   NOT NULL                COMMENT '帖子ID',
    `author_id`  BIGINT   NOT NULL                COMMENT '回复人ID',
    `content`    TEXT     NOT NULL                COMMENT '回复内容',
    `status`     INT      DEFAULT 1               COMMENT '状态：1=正常, 0=已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复表';

-- =====================================
-- Existing database alignment
-- =====================================

-- Spring SQL initialization runs on every startup. These guarded statements
-- upgrade tables created by older revisions without failing on aligned tables.
SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course'
       AND COLUMN_NAME = 'course_code') = 0,
    'ALTER TABLE `course`
       ADD COLUMN `course_code` VARCHAR(16) DEFAULT NULL AFTER `course_name`,
       ADD COLUMN `classification` VARCHAR(8) DEFAULT NULL AFTER `course_code`,
       ADD COLUMN `credit` DECIMAL(3,1) DEFAULT NULL AFTER `classification`,
       ADD COLUMN `weekly_frequency` INT DEFAULT NULL AFTER `credit`,
       ADD COLUMN `prerequisite_id` BIGINT DEFAULT NULL AFTER `weekly_frequency`,
       ADD COLUMN `is_active` TINYINT NOT NULL DEFAULT 1 AFTER `prerequisite_id`,
       ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `is_active`',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'schedule'
       AND COLUMN_NAME = 'week_pattern') = 0,
    'ALTER TABLE `schedule`
       ADD COLUMN `parent_id` BIGINT DEFAULT NULL AFTER `schedule_type`,
       ADD COLUMN `week_pattern` VARCHAR(16) NOT NULL DEFAULT ''every'' AFTER `parent_id`,
       ADD COLUMN `target_grade_id` BIGINT DEFAULT NULL AFTER `week_pattern`,
       ADD KEY `idx_schedule_parent` (`parent_id`),
       ADD KEY `idx_schedule_target_grade` (`target_grade_id`)',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course_capacity'
       AND COLUMN_NAME = 'schedule_id') = 0,
    'ALTER TABLE `course_capacity`
       ADD COLUMN `schedule_id` BIGINT DEFAULT NULL AFTER `min_capacity`,
       ADD KEY `idx_course_capacity_schedule` (`schedule_id`)',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evaluation'
       AND COLUMN_NAME = 'schedule_id') = 0,
    'ALTER TABLE `evaluation`
       ADD COLUMN `schedule_id` BIGINT DEFAULT NULL AFTER `student_id`,
       ADD UNIQUE KEY `uk_evaluation_student_schedule` (`student_id`, `schedule_id`),
       ADD KEY `idx_evaluation_teacher_semester` (`teacher_id`, `semester`),
       ADD KEY `idx_evaluation_teacher_course` (`teacher_id`, `course_id`, `semester`)',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'menu'
       AND COLUMN_NAME = 'permission_code') = 0,
    'ALTER TABLE `menu`
       DROP COLUMN `user_type`,
       ADD COLUMN `permission_code` VARCHAR(64) DEFAULT NULL AFTER `parent_id`,
       ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `permission_code`,
       ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 AFTER `sort_order`,
       ADD UNIQUE KEY `uk_menu_path` (`path`)',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

ALTER TABLE `exam_room`
    MODIFY COLUMN `seat_no` VARCHAR(16) DEFAULT NULL COMMENT 'Seat number';

DELETE duplicate_score
FROM `score` duplicate_score
JOIN `score` retained_score
  ON retained_score.student_id = duplicate_score.student_id
 AND retained_score.course_id = duplicate_score.course_id
 AND retained_score.semester <=> duplicate_score.semester
 AND retained_score.score_id < duplicate_score.score_id;

SET @teaching_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'score'
       AND INDEX_NAME = 'uk_score_student_course_sem') = 0,
    'ALTER TABLE `score`
       ADD UNIQUE KEY `uk_score_student_course_sem`
       (`student_id`, `course_id`, `semester`)',
    'SELECT 1');
PREPARE teaching_stmt FROM @teaching_ddl;
EXECUTE teaching_stmt;
DEALLOCATE PREPARE teaching_stmt;

-- Align databases created before the office workflow extensions.
SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment'
       AND COLUMN_NAME = 'work_plan_id') = 0,
    'ALTER TABLE `payment`
       ADD COLUMN `work_plan_id` BIGINT DEFAULT NULL AFTER `fee_id`,
       MODIFY COLUMN `payment_type` VARCHAR(16) DEFAULT ''学费'',
       ADD UNIQUE KEY `uk_payment_work_plan` (`work_plan_id`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asset'
       AND COLUMN_NAME = 'application_type') = 0,
    'ALTER TABLE `asset`
       ADD COLUMN `application_type` VARCHAR(16) DEFAULT NULL AFTER `apply_user_id`,
       ADD COLUMN `source_asset_id` BIGINT DEFAULT NULL AFTER `application_type`,
       ADD COLUMN `application_reason` VARCHAR(512) DEFAULT NULL AFTER `source_asset_id`,
       ADD COLUMN `approve_user_id` BIGINT DEFAULT NULL AFTER `approve_status`,
       ADD COLUMN `approve_remark` VARCHAR(256) DEFAULT NULL AFTER `approve_user_id`,
       ADD COLUMN `approve_time` DATETIME DEFAULT NULL AFTER `approve_remark`,
       ADD KEY `idx_asset_application` (`application_type`, `approve_status`, `create_time`),
       ADD KEY `idx_asset_source` (`source_asset_id`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_plan'
       AND COLUMN_NAME = 'assigner_id') = 0,
    'ALTER TABLE `work_plan`
       ADD COLUMN `assigner_id` BIGINT DEFAULT NULL AFTER `supervisor_comment`,
       ADD COLUMN `wage_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `assigner_id`,
       ADD COLUMN `wage_paid` TINYINT NOT NULL DEFAULT 0 AFTER `wage_amount`,
       ADD COLUMN `wage_paid_time` DATETIME DEFAULT NULL AFTER `wage_paid`,
       ADD KEY `idx_work_plan_assigner` (`assigner_id`, `status`, `wage_paid`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document'
       AND COLUMN_NAME = 'workflow_id') = 0,
    'ALTER TABLE `document`
       ADD COLUMN `workflow_id` BIGINT DEFAULT NULL AFTER `approval_chain`,
       ADD COLUMN `current_step` INT DEFAULT NULL AFTER `workflow_id`,
       ADD COLUMN `approval_round` INT NOT NULL DEFAULT 1 AFTER `current_step`',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document_approval'
       AND COLUMN_NAME = 'task_id') = 0,
    'ALTER TABLE `document_approval`
       ADD COLUMN `task_id` BIGINT DEFAULT NULL AFTER `doc_id`,
       ADD COLUMN `round_no` INT DEFAULT NULL AFTER `approver_id`,
       ADD COLUMN `step_order` INT DEFAULT NULL AFTER `round_no`,
       ADD COLUMN `step_name` VARCHAR(64) DEFAULT NULL AFTER `step_order`',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

-- =====================================
-- 协同办公 RBAC 基线
-- =====================================
INSERT INTO `role` (`role_code`, `role_name`, `status`) VALUES
    ('STUDENT', '学生', 1),
    ('TEACHER', '教师', 1),
    ('STAFF', '教职工', 1),
    ('COUNSELOR', '辅导员', 1),
    ('LEADER', '校领导', 1),
    ('ADMIN', '系统管理员', 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `status` = 1;

INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('office:read', '读取办公数据', 1),
    ('office:write', '维护办公数据', 1),
    ('fee:self:read', '查询本人账单与流水', 1),
    ('fee:self:pay', '支付本人账单', 1),
    ('fee:manage', '管理费用账单', 1),
    ('fee:overview:read', '查询学生缴费概览', 1),
    ('asset:read', '读取资产台账', 1),
    ('asset:apply', '提交资产申请', 1),
    ('asset:manage', '管理和审批资产', 1),
    ('work-plan:self', '维护本人工作计划', 1),
    ('work-plan:manage', '管理和点评工作计划', 1),
    ('document:self', '发起并查看本人公文', 1),
    ('document:approve', '审批流转至本人的公文', 1),
    ('document:manage', '管理公文审批资格与流程', 1),
    ('meeting:self', '查看并反馈本人会议', 1),
    ('meeting:manage', '发布和管理会议', 1),
    ('notification:self:read', '读取本人通知', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
CROSS JOIN `permission` p
WHERE r.role_code = 'ADMIN'
   OR (r.role_code = 'STUDENT' AND p.permission_code IN (
       'fee:self:read', 'fee:self:pay', 'work-plan:self', 'document:self',
       'notification:self:read'))
   OR (r.role_code = 'TEACHER' AND p.permission_code IN (
       'office:read', 'fee:overview:read', 'asset:read', 'asset:apply',
       'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
       'meeting:self', 'meeting:manage', 'notification:self:read'))
   OR (r.role_code = 'STAFF' AND p.permission_code IN (
       'office:read', 'office:write', 'fee:self:read', 'fee:self:pay', 'fee:manage',
       'fee:overview:read', 'asset:read', 'asset:apply', 'asset:manage',
       'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
       'document:manage', 'meeting:self', 'meeting:manage', 'notification:self:read'))
   OR (r.role_code = 'LEADER' AND p.permission_code IN (
       'office:read', 'fee:overview:read'));

INSERT INTO `menu` (`title`, `path`, `permission_code`, `sort_order`, `status`) VALUES
    ('学杂费交纳', '/home/fee-payment', 'fee:self:read', 31, 1),
    ('固定资产管理', '/home/asset-management', 'asset:read', 32, 1),
    ('工作计划', '/home/work-plan', 'work-plan:self', 33, 1),
    ('公文流转 OA', '/home/document-oa', 'document:self', 34, 1),
    ('会议与通知', '/home/meeting-notice', 'notification:self:read', 35, 1)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `permission_code` = VALUES(`permission_code`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);

-- =====================================
-- 测试种子数据（含学生/教师/成绩/选课）
-- =====================================
DELETE FROM score WHERE 1=1;
DELETE FROM course_selection WHERE 1=1;
DELETE FROM schedule WHERE 1=1;
DELETE FROM course_capacity WHERE 1=1;
DELETE FROM course WHERE 1=1;
DELETE FROM classroom WHERE 1=1;
DELETE FROM student WHERE 1=1;
DELETE FROM user WHERE username LIKE '60%' AND user_id > 2;
DELETE FROM user WHERE username = 'teacher01' OR username = 'teacher02';

-- 教师账号（教职工 user_type=3）
INSERT INTO user (username, password, user_type, status) VALUES
('teacher01', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, 1),
('teacher02', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, 1)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 学生账号（user_type=1，学号 600001-600010）
INSERT INTO user (username, password, user_type, status) VALUES
('600001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600002', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600003', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600004', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600005', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600006', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600007', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600008', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600009', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
('600010', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 学生档案
INSERT INTO student (student_id, student_name, student_no, grade_id, student_age) VALUES
(2,  '林同学', 600001, 1, 20),
(6,  '张伟',   600002, 1, 19),
(7,  '李娜',   600003, 1, 20),
(8,  '王强',   600004, 1, 21),
(9,  '赵敏',   600005, 2, 19),
(10, '陈静',   600006, 2, 20),
(11, '刘洋',   600007, 2, 21),
(12, '周杰',   600008, 3, 19),
(13, '吴芳',   600009, 3, 20),
(14, '孙鹏',   600010, 3, 21)
ON DUPLICATE KEY UPDATE student_name=VALUES(student_name);

-- 学生角色分配
INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id FROM user u JOIN role r ON
    u.username LIKE '60%' AND r.role_code = 'STUDENT';

-- 教室
INSERT INTO classroom (classroom_id, classroom_name, building, capacity, type, status) VALUES
(1, '教学楼A101', 'A栋', 60,  '普通教室', 1),
(2, '教学楼A102', 'A栋', 45,  '普通教室', 1),
(3, '教学楼B201', 'B栋', 80,  '多媒体',   1),
(4, '实验楼C101', 'C栋', 30,  '实验室',   1),
(5, '实验楼C102', 'C栋', 30,  '实验室',   1),
(6, '阶梯教室D101','D栋', 120, '阶梯教室', 1)
ON DUPLICATE KEY UPDATE classroom_name=VALUES(classroom_name);

-- 课程
INSERT INTO course (course_id, course_name, course_code, classification, credit, weekly_frequency, is_active) VALUES
(1,  '高等数学(上)',      'MATH101', '必修', 5, 2, 1),
(2,  '线性代数',          'MATH102', '必修', 3, 2, 1),
(3,  '大学物理',          'PHYS101', '必修', 4, 2, 1),
(4,  '大学英语(三)',      'ENGL101', '必修', 3, 2, 1),
(5,  '数据结构与算法',    'CS201',   '必修', 4, 2, 1),
(6,  '操作系统',          'CS301',   '必修', 3, 1, 1),
(7,  '马克思主义原理',    'POLI101', '必修', 2, 1, 1),
(8,  '大学体育(三)',      'PE101',   '必修', 1, 1, 1),
(9,  'Python程序设计',    'CS105',   '选修', 2, 1, 1),
(10, '数据库原理与应用',  'CS202',   '限选', 3, 2, 1)
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

-- 容量（固定主键与排课首段关联，确保重复启动不会追加数据）
INSERT INTO course_capacity
    (capacity_id, course_id, semester, max_capacity, current_count, min_capacity, schedule_id) VALUES
(1,1,'2025-2026-1',60,50,15,1),(2,2,'2025-2026-1',50,45,15,3),
(3,3,'2025-2026-1',55,48,15,4),(4,4,'2025-2026-1',45,42,15,5),
(5,5,'2025-2026-1',40,38,15,6),(6,6,'2025-2026-1',35,30,10,7),
(7,7,'2025-2026-1',80,78,20,8),(8,8,'2025-2026-1',30,28,10,9),
(9,9,'2025-2026-1',60,55,15,10),(10,10,'2025-2026-1',40,35,10,11)
ON DUPLICATE KEY UPDATE
    course_id=VALUES(course_id), semester=VALUES(semester),
    max_capacity=VALUES(max_capacity), current_count=VALUES(current_count),
    min_capacity=VALUES(min_capacity), schedule_id=VALUES(schedule_id);

-- 排课（固定主键供选课与容量种子引用；第二段高数排课关联首段）
INSERT INTO schedule
    (schedule_id, course_id, classroom_id, teacher_id, semester, week_day,
     start_period, end_period, start_week, end_week, schedule_type,
     parent_id, week_pattern, target_grade_id) VALUES
(1, 1, 6, 3, '2025-2026-1', 1, 1, 2, 1, 16, '正常', NULL, 'every', NULL),
(2, 1, 6, 3, '2025-2026-1', 3, 3, 5, 1, 16, '正常', 1,    'every', NULL),
(3, 2, 1, 3, '2025-2026-1', 2, 3, 5, 1, 16, '正常', NULL, 'every', NULL),
(4, 3, 3, 3, '2025-2026-1', 3, 8,10, 1, 16, '正常', NULL, 'every', NULL),
(5, 4, 2, 4, '2025-2026-1', 4, 3, 5, 1, 16, '正常', NULL, 'every', NULL),
(6, 5, 3, 4, '2025-2026-1', 5, 8,10, 1, 16, '正常', NULL, 'every', NULL),
(7, 6, 1, 3, '2025-2026-1', 1, 8,10, 1, 16, '正常', NULL, 'every', NULL),
(8, 7, 6, 4, '2025-2026-1', 2, 1, 2, 1, 16, '正常', NULL, 'every', NULL),
(9, 8, 4, 5, '2025-2026-1', 5, 6, 7, 1, 16, '正常', NULL, 'every', NULL),
(10,9, 5, 4, '2025-2026-1', 3, 1, 2, 1, 16, '正常', NULL, 'every', 1),
(11,10,1, 3, '2025-2026-1', 4, 8,10, 1, 16, '正常', NULL, 'every', 1)
ON DUPLICATE KEY UPDATE
    course_id=VALUES(course_id), classroom_id=VALUES(classroom_id),
    teacher_id=VALUES(teacher_id), semester=VALUES(semester),
    week_day=VALUES(week_day), start_period=VALUES(start_period),
    end_period=VALUES(end_period), start_week=VALUES(start_week),
    end_week=VALUES(end_week), schedule_type=VALUES(schedule_type),
    parent_id=VALUES(parent_id), week_pattern=VALUES(week_pattern),
    target_grade_id=VALUES(target_grade_id);

-- 选课（学生2=600001, 6-14=600002-600010）
INSERT INTO course_selection (student_id, course_id, schedule_id, semester, status, select_time) VALUES
(2, 1, 1, '2025-2026-1', 1, NOW()),(2, 2, 3, '2025-2026-1', 1, NOW()),
(2, 4, 5, '2025-2026-1', 1, NOW()),(2, 7, 8, '2025-2026-1', 1, NOW()),
(2, 9, 10,'2025-2026-1', 1, NOW()),
(6, 1, 1, '2025-2026-1', 1, NOW()),(6, 3, 4, '2025-2026-1', 1, NOW()),
(6, 5, 6, '2025-2026-1', 1, NOW()),(6, 8, 9, '2025-2026-1', 1, NOW()),
(7, 2, 3, '2025-2026-1', 1, NOW()),(7, 4, 5, '2025-2026-1', 1, NOW()),
(7, 6, 7, '2025-2026-1', 1, NOW()),(7,10,11,'2025-2026-1', 1, NOW()),
(8, 1, 1, '2025-2026-1', 1, NOW()),(8, 9,10,'2025-2026-1', 1, NOW()),
(9, 3, 4, '2025-2026-1', 1, NOW()),(9, 5, 6, '2025-2026-1', 1, NOW()),
(9, 7, 8, '2025-2026-1', 1, NOW()),
(10,2, 3, '2025-2026-1', 1, NOW()),(10,4, 5,'2025-2026-1', 1, NOW()),
(10,6, 7, '2025-2026-1', 1, NOW()),
(11,1, 1, '2025-2026-1', 1, NOW()),(11,8, 9,'2025-2026-1', 1, NOW()),
(12,5, 6, '2025-2026-1', 1, NOW()),(12,10,11,'2025-2026-1', 1, NOW()),
(13,3, 4, '2025-2026-1', 1, NOW()),(13,7, 8,'2025-2026-1', 1, NOW()),
(14,2, 3, '2025-2026-1', 1, NOW()),(14,9,10,'2025-2026-1', 1, NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 成绩（含已发布和未发布）
INSERT INTO score (student_id, course_id, score_score, semester, gpa, status) VALUES
-- 600001 林同学: 全部及格
(2, 1, 88, '2025-2026-1', 3.3, 1),(2, 2, 76, '2025-2026-1', 2.3, 1),
(2, 4, 82, '2025-2026-1', 3.3, 1),(2, 7, 90, '2025-2026-1', 4.0, 1),
(2, 9, 65, '2025-2026-1', 1.5, 1),
-- 600002 张伟: 1门不及格
(6, 1, 55, '2025-2026-1', 0.0, 0),(6, 3, 72, '2025-2026-1', 2.0, 1),
(6, 5, 81, '2025-2026-1', 3.0, 1),(6, 8, 78, '2025-2026-1', 3.0, 1),
-- 600003 李娜: 全部高分
(7, 2, 95, '2025-2026-1', 4.0, 1),(7, 4, 88, '2025-2026-1', 3.3, 1),
(7, 6, 91, '2025-2026-1', 4.0, 1),(7,10, 85, '2025-2026-1', 3.7, 1),
-- 600004 王强: 2门不及格(预警)
(8, 1, 48, '2025-2026-1', 0.0, 0),(8, 9, 52, '2025-2026-1', 0.0, 0),
-- 600005 赵敏: 正常
(9, 3, 74, '2025-2026-1', 2.0, 1),(9, 5, 68, '2025-2026-1', 2.0, 1),
(9, 7, 83, '2025-2026-1', 3.3, 1),
-- 600006 陈静: 1门不及格
(10,2, 58, '2025-2026-1', 0.0, 0),(10,4, 77, '2025-2026-1', 2.3, 1),
(10,6, 79, '2025-2026-1', 2.3, 1),
-- 600007 刘洋: 3门不及格(红色预警,学期14学分)
(11,1, 45, '2025-2026-1', 0.0, 0),(11,8, 50, '2025-2026-1', 0.0, 0),
-- 600008 周杰: 正常
(12,5, 86, '2025-2026-1', 3.3, 1),(12,10,71,'2025-2026-1', 2.0, 1),
-- 600009 吴芳: 正常
(13,3, 80, '2025-2026-1', 3.0, 1),(13,7, 66, '2025-2026-1', 1.5, 1),
-- 600010 孙鹏: 1门不及格
(14,2, 59, '2025-2026-1', 0.0, 0),(14,9, 73, '2025-2026-1', 2.0, 1)
ON DUPLICATE KEY UPDATE
    score_score=VALUES(score_score), gpa=VALUES(gpa), status=VALUES(status);


