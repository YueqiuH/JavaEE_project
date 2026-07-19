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
    `role_id`      BIGINT NOT NULL,
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
    `gender`          TINYINT      DEFAULT NULL            COMMENT '性别: 1=男, 2=女',
    `dept_id`         BIGINT       DEFAULT NULL            COMMENT '所属院系ID',
    `major_id`        BIGINT       DEFAULT NULL            COMMENT '所属专业ID',
    `class_name`      VARCHAR(32)  DEFAULT NULL            COMMENT '班级',
    `origin_place`    VARCHAR(32)  DEFAULT NULL            COMMENT '生源地(省份)',
    `enroll_year`     INT          DEFAULT NULL            COMMENT '入学年份',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学',
    PRIMARY KEY (`student_id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_student_dept_major` (`dept_id`, `major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `course_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
    `course_name` VARCHAR(64)  NOT NULL                COMMENT '课程名称',
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
    PRIMARY KEY (`dept_id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- 专业表
CREATE TABLE IF NOT EXISTS `major` (
    `major_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专业主键ID',
    `dept_id`    BIGINT       NOT NULL                COMMENT '所属院系ID',
    `major_name` VARCHAR(64)  NOT NULL                COMMENT '专业名称',
    `major_code` VARCHAR(16)  DEFAULT NULL            COMMENT '专业编号',
    `cultivation_plan` TEXT   DEFAULT NULL            COMMENT '培养方案',
    PRIMARY KEY (`major_id`),
    UNIQUE KEY `uk_major_code` (`major_code`)
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
    PRIMARY KEY (`schedule_id`)
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
    PRIMARY KEY (`capacity_id`)
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
    PRIMARY KEY (`score_id`)
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
    `application_no` VARCHAR(32) NOT NULL                COMMENT '申请编号',
    `student_id`    BIGINT       NOT NULL                COMMENT '学生ID',
    `change_type`   VARCHAR(32)  NOT NULL                COMMENT '类型：SUSPENSION/RESUMPTION/MAJOR_CHANGE/WITHDRAWAL',
    `reason`        VARCHAR(512) NOT NULL                COMMENT '申请原因',
    `new_major_id`  BIGINT       DEFAULT NULL            COMMENT '转专业-新专业ID',
    `desired_effective_date` DATE NOT NULL                COMMENT '期望生效日期',
    `status`        INT          NOT NULL DEFAULT 0       COMMENT '0=草稿,1=辅导员初审,2=教务复审,3=通过,4=初审拒绝,5=复审拒绝,6=撤回',
    `counselor_id`  BIGINT       DEFAULT NULL            COMMENT '辅导员ID',
    `counselor_opinion` VARCHAR(256) DEFAULT NULL        COMMENT '辅导员意见',
    `counselor_reviewed_at` DATETIME DEFAULT NULL         COMMENT '辅导员审核时间',
    `academic_reviewer_id` BIGINT DEFAULT NULL            COMMENT '教务复审人ID',
    `academic_opinion` VARCHAR(256) DEFAULT NULL          COMMENT '教务复审意见',
    `academic_reviewed_at` DATETIME DEFAULT NULL          COMMENT '教务复审时间',
    `apply_time`    DATETIME     DEFAULT NULL             COMMENT '提交时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`change_id`),
    UNIQUE KEY `uk_status_change_application_no` (`application_no`),
    KEY `idx_status_change_student_status` (`student_id`, `status`),
    KEY `idx_status_change_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学籍异动申请表';

-- 学生非核心联系信息扩展表（成员 B 维护，不改写共享 student 基础档案）
CREATE TABLE IF NOT EXISTS `student_profile_extension` (
    `student_id` BIGINT NOT NULL,
    `current_address` VARCHAR(128) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(128) DEFAULT NULL,
    `emergency_contact` VARCHAR(32) DEFAULT NULL,
    `emergency_phone` VARCHAR(20) DEFAULT NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生非核心联系信息扩展表';

-- 奖助贷申请表
CREATE TABLE IF NOT EXISTS `scholarship` (
    `scholarship_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请主键ID',
    `application_no`   VARCHAR(32)  NOT NULL                COMMENT '申请编号',
    `student_id`       BIGINT       NOT NULL                COMMENT '学生ID',
    `scholarship_type` VARCHAR(32)  NOT NULL                COMMENT '类型：SCHOLARSHIP/DIFFICULTY_GRANT/STUDENT_LOAN',
    `title`            VARCHAR(128) NOT NULL                COMMENT '申请标题',
    `reason`           TEXT         NOT NULL                COMMENT '申请理由',
    `attachment_url`   VARCHAR(256) DEFAULT NULL            COMMENT '附件地址',
    `status`           INT          NOT NULL DEFAULT 0      COMMENT '状态：0=草稿,1=已提交,2=已退回,3=已通过,4=已拒绝,5=已撤回,6=已入选',
    `reviewer_id`      BIGINT       DEFAULT NULL            COMMENT '审核人ID',
    `review_opinion`   VARCHAR(256) DEFAULT NULL            COMMENT '审核意见',
    `apply_time`       DATETIME     DEFAULT NULL             COMMENT '提交时间',
    `reviewed_at`      DATETIME     DEFAULT NULL             COMMENT '评审时间',
    `selected_at`      DATETIME     DEFAULT NULL             COMMENT '入选时间',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`scholarship_id`),
    UNIQUE KEY `uk_scholarship_application_no` (`application_no`),
    KEY `idx_scholarship_student_status` (`student_id`, `status`),
    KEY `idx_scholarship_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖助贷申请表';

-- 评教表
CREATE TABLE IF NOT EXISTS `evaluation` (
    `evaluation_id` BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评教主键ID',
    `student_id`    BIGINT   NOT NULL                COMMENT '学生ID',
    `teacher_id`    BIGINT   NOT NULL                COMMENT '被评教师ID',
    `course_id`     BIGINT   NOT NULL                COMMENT '课程ID',
    `semester`      VARCHAR(32) NOT NULL             COMMENT '学期',
    `score_teaching` INT     DEFAULT NULL            COMMENT '教学态度评分 1-5',
    `score_content` INT      DEFAULT NULL            COMMENT '教学内容评分 1-5',
    `score_method`  INT      DEFAULT NULL            COMMENT '教学方法评分 1-5',
    `comment`       TEXT     DEFAULT NULL            COMMENT '匿名评价',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`evaluation_id`)
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
    `application_type` VARCHAR(16) DEFAULT NULL          COMMENT '申请类型：PURCHASE=购买, ADD=添加, BORROW=借用, SCRAP=报废, LEGACY=历史；台账为空',
    `source_asset_id` BIGINT     DEFAULT NULL            COMMENT '借用或报废申请对应的来源资产ID',
    `application_reason` VARCHAR(512) DEFAULT NULL       COMMENT '申请人填写的损坏情况和报废原因',
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

-- 公文审批资格配置表（由管理员维护，学生不得加入）
CREATE TABLE IF NOT EXISTS `document_approver` (
    `approver_config_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审批人配置主键ID',
    `user_id`            BIGINT       NOT NULL                COMMENT '审批用户ID',
    `display_name`       VARCHAR(32)  NOT NULL                COMMENT '审批人显示名称',
    `status`             TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用, 0=停用',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by`         BIGINT       DEFAULT NULL            COMMENT '最后操作管理员ID',
    `updated_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`approver_config_id`),
    UNIQUE KEY `uk_document_approver_user` (`user_id`),
    CONSTRAINT `fk_document_approver_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批资格配置表';

-- 公文审批流程版本表
CREATE TABLE IF NOT EXISTS `document_workflow` (
    `workflow_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流程版本主键ID',
    `workflow_name` VARCHAR(64)  NOT NULL                COMMENT '流程名称',
    `doc_type`      VARCHAR(16)  NOT NULL                COMMENT '公文类型',
    `version`       INT          NOT NULL                COMMENT '同类型流程版本号',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=当前启用, 0=历史版本',
    `created_by`    BIGINT       NOT NULL                COMMENT '配置管理员ID',
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
    PRIMARY KEY (`enrollment_id`),
    UNIQUE KEY `uk_enrollment_major_year` (`major_id`, `year`)
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

-- ============================================
-- 本地演示认证数据（统一密码: 123321）
-- ============================================

INSERT INTO `role` (`role_code`, `role_name`) VALUES
    ('STUDENT', '学生'),
    ('TEACHER', '教师'),
    ('STAFF', '教职工'),
    ('ADMIN', '系统管理员')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('teaching:read', '读取教务数据'), ('teaching:write', '维护教务数据'),
    ('student:read', '读取学生事务'), ('student:write', '维护学生事务'),
    ('office:read', '读取办公数据'), ('office:write', '维护办公数据'),
    ('base:read', '读取基础数据'), ('base:write', '维护基础数据'),
    ('admin:manage', '系统管理'),
    ('scholarship:application:read-self', '查看本人奖助贷申请'),
    ('scholarship:application:create', '创建奖助贷申请'),
    ('scholarship:application:update-self', '修改本人奖助贷申请'),
    ('scholarship:application:submit-self', '提交本人奖助贷申请'),
    ('scholarship:application:withdraw-self', '撤回本人奖助贷申请'),
    ('scholarship:review:read', '查看奖助贷评审队列'),
    ('scholarship:review:submit', '提交奖助贷评审结论'),
    ('scholarship:result:generate', '生成奖助贷资助名单'),
    ('status:profile:read-self', '查看本人非核心信息'),
    ('status:profile:update-self', '修改本人非核心信息'),
    ('status:change:read-self', '查看本人学籍异动申请'),
    ('status:change:create', '创建学籍异动申请'),
    ('status:change:update-self', '修改本人学籍异动草稿'),
    ('status:change:submit-self', '提交本人学籍异动申请'),
    ('status:change:withdraw-self', '撤回本人学籍异动申请'),
    ('status:review:read', '查看学籍异动审核队列'),
    ('status:review:submit', '提交学籍异动审核结论'),
    ('fee:self:read', '查询本人账单与流水'), ('fee:self:pay', '支付本人账单'), ('fee:manage', '管理费用账单'),
    ('fee:overview:read', '查询学生缴费概览'),
    ('asset:read', '读取资产台账'), ('asset:apply', '提交资产申请'), ('asset:manage', '管理员管理和审批资产'),
    ('work-plan:self', '维护本人工作计划'), ('work-plan:manage', '管理和点评工作计划'),
    ('document:self', '发起并查看本人公文'), ('document:approve', '审批流转至本人的公文'),
    ('document:manage', '管理公文审批资格与流程'),
    ('meeting:self', '查看并反馈本人会议'), ('meeting:manage', '发布和管理会议'),
    ('notification:self:read', '读取本人通知')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT INTO `user` (`username`, `password`, `user_type`, `status`) VALUES
    ('600001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
    ('700001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, 1),
    ('800001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, 1),
    ('admin', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 4, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `user_type` = VALUES(`user_type`), `status` = VALUES(`status`);

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON
    (u.username = '600001' AND r.role_code = 'STUDENT') OR
    (u.username = '700001' AND r.role_code = 'TEACHER') OR
    (u.username = '800001' AND r.role_code = 'STAFF') OR
    (u.username = 'admin' AND r.role_code = 'ADMIN');

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id FROM `role` r CROSS JOIN `permission` p WHERE
    (r.role_code = 'ADMIN' AND p.permission_code <> 'fee:overview:read')
    OR (r.role_code = 'STUDENT' AND p.permission_code IN (
        'teaching:read', 'student:read', 'student:write', 'base:read',
        'scholarship:application:read-self', 'scholarship:application:create',
        'scholarship:application:update-self', 'scholarship:application:submit-self',
        'scholarship:application:withdraw-self',
        'status:profile:read-self', 'status:profile:update-self', 'status:change:read-self',
        'status:change:create', 'status:change:update-self', 'status:change:submit-self',
        'status:change:withdraw-self',
        'fee:self:read', 'fee:self:pay', 'work-plan:self', 'document:self', 'notification:self:read'))
    OR (r.role_code = 'TEACHER' AND p.permission_code IN (
        'teaching:read', 'teaching:write', 'student:read', 'base:read', 'office:read',
        'scholarship:review:read', 'scholarship:review:submit', 'scholarship:result:generate',
        'status:review:read', 'status:review:submit',
        'fee:overview:read',
        'asset:read', 'asset:apply', 'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
        'meeting:self', 'meeting:manage', 'notification:self:read'))
    OR (r.role_code = 'STAFF' AND p.permission_code IN (
        'student:read', 'student:write', 'office:read', 'office:write', 'base:read',
        'fee:self:read', 'fee:self:pay', 'fee:manage',
        'asset:read', 'asset:apply',
        'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
        'meeting:self', 'meeting:manage', 'notification:self:read'));

INSERT INTO `document_approver` (`user_id`, `display_name`, `status`)
SELECT u.user_id,
       CASE u.username WHEN '700001' THEN '教学负责人' ELSE '行政负责人' END,
       1
FROM `user` u
WHERE u.username IN ('700001', '800001')
ON DUPLICATE KEY UPDATE `display_name` = VALUES(`display_name`), `status` = 1;

INSERT INTO `student` (`student_name`, `student_no`, `student_age`)
SELECT '演示学生', 600001, 20
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = 600001);

INSERT INTO `menu` (`title`, `path`, `permission_code`, `sort_order`) VALUES
    ('教务核心', '/home/course-schedule', 'teaching:read', 10),
    ('学生事务', '/home/student-status', 'student:read', 20),
    ('协同办公', '/home/work-plan', 'office:read', 30),
    ('学杂费交纳', '/home/fee-payment', 'fee:self:read', 31),
    ('基础数据', '/home/user-management', 'base:read', 40)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `permission_code` = VALUES(`permission_code`), `sort_order` = VALUES(`sort_order`);

-- ============================================
-- 成员 D：基础数据种子
-- 表结构（user/student 档案字段、department/major/enrollment 唯一键）已并入上方建表语句，
-- 新建库无需执行 migration/base/V20260717100000__base_profile_columns.sql。
-- 演示种子数据（院系/专业/师生档案/招生计划/新闻/论坛）请在本脚本之后执行：
--   database/migration/base/V20260717100500__base_seed_data.sql
-- ============================================

-- C4 公文固定流程演示数据（幂等）
-- ============================================

-- 为三类公文建立管理员固定流程。已存在同类型流程时不覆盖管理员配置。
INSERT INTO `document_workflow`
    (`workflow_name`, `doc_type`, `version`, `status`, `created_by`, `create_time`)
SELECT '公文会签两级审批', '公文会签', 1, 1, admin_user.user_id, NOW()
FROM `user` admin_user
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document_workflow` WHERE `doc_type` = '公文会签');

INSERT INTO `document_workflow`
    (`workflow_name`, `doc_type`, `version`, `status`, `created_by`, `create_time`)
SELECT '请示报告两级审批', '请示报告', 1, 1, admin_user.user_id, NOW()
FROM `user` admin_user
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document_workflow` WHERE `doc_type` = '请示报告');

INSERT INTO `document_workflow`
    (`workflow_name`, `doc_type`, `version`, `status`, `created_by`, `create_time`)
SELECT '请假申请单级审批', '请假申请', 1, 1, admin_user.user_id, NOW()
FROM `user` admin_user
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document_workflow` WHERE `doc_type` = '请假申请');

-- 公文会签：教学负责人初审 -> 行政负责人会签。
INSERT INTO `document_workflow_step` (`workflow_id`, `step_order`, `step_name`, `approver_id`)
SELECT workflow.workflow_id, 1, '教学负责人初审', teacher_user.user_id
FROM `document_workflow` workflow
JOIN `user` teacher_user ON teacher_user.username = '700001'
WHERE workflow.doc_type = '公文会签' AND workflow.workflow_name = '公文会签两级审批'
  AND workflow.version = 1 AND workflow.status = 1
  AND NOT EXISTS (SELECT 1 FROM `document_workflow_step` step
                  WHERE step.workflow_id = workflow.workflow_id AND step.step_order = 1);

INSERT INTO `document_workflow_step` (`workflow_id`, `step_order`, `step_name`, `approver_id`)
SELECT workflow.workflow_id, 2, '行政负责人会签', staff_user.user_id
FROM `document_workflow` workflow
JOIN `user` staff_user ON staff_user.username = '800001'
WHERE workflow.doc_type = '公文会签' AND workflow.workflow_name = '公文会签两级审批'
  AND workflow.version = 1 AND workflow.status = 1
  AND NOT EXISTS (SELECT 1 FROM `document_workflow_step` step
                  WHERE step.workflow_id = workflow.workflow_id AND step.step_order = 2);

-- 请示报告：行政负责人初审 -> 教学负责人复核。
INSERT INTO `document_workflow_step` (`workflow_id`, `step_order`, `step_name`, `approver_id`)
SELECT workflow.workflow_id, 1, '行政负责人初审', staff_user.user_id
FROM `document_workflow` workflow
JOIN `user` staff_user ON staff_user.username = '800001'
WHERE workflow.doc_type = '请示报告' AND workflow.workflow_name = '请示报告两级审批'
  AND workflow.version = 1 AND workflow.status = 1
  AND NOT EXISTS (SELECT 1 FROM `document_workflow_step` step
                  WHERE step.workflow_id = workflow.workflow_id AND step.step_order = 1);

INSERT INTO `document_workflow_step` (`workflow_id`, `step_order`, `step_name`, `approver_id`)
SELECT workflow.workflow_id, 2, '教学负责人复核', teacher_user.user_id
FROM `document_workflow` workflow
JOIN `user` teacher_user ON teacher_user.username = '700001'
WHERE workflow.doc_type = '请示报告' AND workflow.workflow_name = '请示报告两级审批'
  AND workflow.version = 1 AND workflow.status = 1
  AND NOT EXISTS (SELECT 1 FROM `document_workflow_step` step
                  WHERE step.workflow_id = workflow.workflow_id AND step.step_order = 2);

-- 请假申请：行政负责人审批。
INSERT INTO `document_workflow_step` (`workflow_id`, `step_order`, `step_name`, `approver_id`)
SELECT workflow.workflow_id, 1, '行政负责人审批', staff_user.user_id
FROM `document_workflow` workflow
JOIN `user` staff_user ON staff_user.username = '800001'
WHERE workflow.doc_type = '请假申请' AND workflow.workflow_name = '请假申请单级审批'
  AND workflow.version = 1 AND workflow.status = 1
  AND NOT EXISTS (SELECT 1 FROM `document_workflow_step` step
                  WHERE step.workflow_id = workflow.workflow_id AND step.step_order = 1);

-- 待行政负责人初审的请示报告。
INSERT INTO `document`
    (`title`, `doc_type`, `content`, `initiator_id`, `current_approver_id`, `status`,
     `approval_chain`, `workflow_id`, `current_step`, `approval_round`, `create_time`)
SELECT '【演示】智慧教室设备采购请示', '请示报告',
       '申请采购智慧教室终端和配套显示设备，用于新学期课堂教学。',
       admin_user.user_id, staff_user.user_id, 0,
       CONCAT('[', staff_user.user_id, ',', teacher_user.user_id, ']'),
       workflow.workflow_id, 1, 1, NOW() - INTERVAL 4 HOUR
FROM `user` admin_user
JOIN `user` staff_user ON staff_user.username = '800001'
JOIN `user` teacher_user ON teacher_user.username = '700001'
JOIN `document_workflow` workflow ON workflow.doc_type = '请示报告'
    AND workflow.workflow_name = '请示报告两级审批' AND workflow.version = 1 AND workflow.status = 1
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document` WHERE `title` = '【演示】智慧教室设备采购请示');

-- 已完成第一步、待教学负责人复核的请示报告。
INSERT INTO `document`
    (`title`, `doc_type`, `content`, `initiator_id`, `current_approver_id`, `status`,
     `approval_chain`, `workflow_id`, `current_step`, `approval_round`, `create_time`)
SELECT '【演示】在线精品课程建设请示', '请示报告',
       '申请启动在线精品课程建设，并安排课程资源录制和教学团队培训。',
       admin_user.user_id, teacher_user.user_id, 0,
       CONCAT('[', staff_user.user_id, ',', teacher_user.user_id, ']'),
       workflow.workflow_id, 2, 1, NOW() - INTERVAL 1 DAY
FROM `user` admin_user
JOIN `user` staff_user ON staff_user.username = '800001'
JOIN `user` teacher_user ON teacher_user.username = '700001'
JOIN `document_workflow` workflow ON workflow.doc_type = '请示报告'
    AND workflow.workflow_name = '请示报告两级审批' AND workflow.version = 1 AND workflow.status = 1
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document` WHERE `title` = '【演示】在线精品课程建设请示');

-- 已通过的请假申请。
INSERT INTO `document`
    (`title`, `doc_type`, `content`, `initiator_id`, `current_approver_id`, `status`,
     `approval_chain`, `workflow_id`, `current_step`, `approval_round`, `create_time`)
SELECT '【演示】外出培训请假申请', '请假申请',
       '因参加高校数字化建设培训，申请外出两天。',
       admin_user.user_id, NULL, 1, CONCAT('[', staff_user.user_id, ']'),
       workflow.workflow_id, 1, 1, NOW() - INTERVAL 2 DAY
FROM `user` admin_user
JOIN `user` staff_user ON staff_user.username = '800001'
JOIN `document_workflow` workflow ON workflow.doc_type = '请假申请'
    AND workflow.workflow_name = '请假申请单级审批' AND workflow.version = 1 AND workflow.status = 1
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document` WHERE `title` = '【演示】外出培训请假申请');

-- 被退回的公文会签，用于测试修改后按原快照重提。
INSERT INTO `document`
    (`title`, `doc_type`, `content`, `initiator_id`, `current_approver_id`, `status`,
     `approval_chain`, `workflow_id`, `current_step`, `approval_round`, `create_time`)
SELECT '【演示】教学管理制度修订会签', '公文会签',
       '提交新版教学管理制度草案，请相关负责人会签。',
       admin_user.user_id, NULL, 3,
       CONCAT('[', teacher_user.user_id, ',', staff_user.user_id, ']'),
       workflow.workflow_id, 1, 1, NOW() - INTERVAL 3 DAY
FROM `user` admin_user
JOIN `user` teacher_user ON teacher_user.username = '700001'
JOIN `user` staff_user ON staff_user.username = '800001'
JOIN `document_workflow` workflow ON workflow.doc_type = '公文会签'
    AND workflow.workflow_name = '公文会签两级审批' AND workflow.version = 1 AND workflow.status = 1
WHERE admin_user.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `document` WHERE `title` = '【演示】教学管理制度修订会签');

-- 根据演示公文生成任务快照。状态：0等待、1待审批、2同意、4退回、5取消。
INSERT INTO `document_approval_task`
    (`doc_id`, `workflow_id`, `round_no`, `step_order`, `step_name`, `approver_id`, `status`, `handled_time`, `create_time`)
SELECT document.doc_id, document.workflow_id, 1, step.step_order, step.step_name, step.approver_id,
       CASE document.title
           WHEN '【演示】智慧教室设备采购请示' THEN IF(step.step_order = 1, 1, 0)
           WHEN '【演示】在线精品课程建设请示' THEN IF(step.step_order = 1, 2, 1)
           WHEN '【演示】外出培训请假申请' THEN 2
           WHEN '【演示】教学管理制度修订会签' THEN IF(step.step_order = 1, 4, 5)
       END,
       CASE
           WHEN document.title = '【演示】在线精品课程建设请示' AND step.step_order = 1 THEN NOW() - INTERVAL 20 HOUR
           WHEN document.title = '【演示】外出培训请假申请' THEN NOW() - INTERVAL 40 HOUR
           WHEN document.title = '【演示】教学管理制度修订会签' AND step.step_order = 1 THEN NOW() - INTERVAL 60 HOUR
           ELSE NULL
       END,
       document.create_time
FROM `document` document
JOIN `document_workflow_step` step ON step.workflow_id = document.workflow_id
WHERE document.title IN (
        '【演示】智慧教室设备采购请示', '【演示】在线精品课程建设请示',
        '【演示】外出培训请假申请', '【演示】教学管理制度修订会签')
  AND NOT EXISTS (SELECT 1 FROM `document_approval_task` task
                  WHERE task.doc_id = document.doc_id AND task.round_no = 1 AND task.step_order = step.step_order);

-- 为已处理任务生成审批意见历史。
INSERT INTO `document_approval`
    (`doc_id`, `task_id`, `approver_id`, `round_no`, `step_order`, `step_name`, `action`, `opinion`, `approval_time`)
SELECT document.doc_id, task.task_id, task.approver_id, task.round_no, task.step_order, task.step_name,
       CASE document.title WHEN '【演示】教学管理制度修订会签' THEN '退回' ELSE '同意' END,
       CASE document.title
           WHEN '【演示】在线精品课程建设请示' THEN '同意建设，请教学负责人复核课程方案。'
           WHEN '【演示】外出培训请假申请' THEN '同意外出培训，请按时返校。'
           WHEN '【演示】教学管理制度修订会签' THEN '请补充制度实施日期和责任部门后重新提交。'
       END,
       task.handled_time
FROM `document` document
JOIN `document_approval_task` task ON task.doc_id = document.doc_id AND task.round_no = 1 AND task.step_order = 1
WHERE document.title IN (
        '【演示】在线精品课程建设请示', '【演示】外出培训请假申请', '【演示】教学管理制度修订会签')
  AND NOT EXISTS (SELECT 1 FROM `document_approval` approval WHERE approval.task_id = task.task_id);

-- 为两份待审批公文生成通知。
INSERT INTO `notification` (`user_id`, `title`, `content`, `notify_type`, `is_read`, `create_time`)
SELECT document.current_approver_id, '演示待审批公文', CONCAT('《', document.title, '》等待您的审批'),
       '公文通知', 0, document.create_time
FROM `document` document
WHERE document.title IN ('【演示】智慧教室设备采购请示', '【演示】在线精品课程建设请示')
  AND document.status = 0
  AND NOT EXISTS (SELECT 1 FROM `notification` notification
                  WHERE notification.user_id = document.current_approver_id
                    AND notification.content = CONCAT('《', document.title, '》等待您的审批'));
