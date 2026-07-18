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
    `user_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    `username`  VARCHAR(32)  NOT NULL                COMMENT '用户名/账号',
    `password`  VARCHAR(32)  NOT NULL                COMMENT '密码',
    `user_type` INT          NOT NULL DEFAULT 1      COMMENT '用户类型: 1=学生, 2=老师, 3=教务, 4=管理员',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

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
    `course_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
    `course_name` VARCHAR(64)  NOT NULL                COMMENT '课程名称',
    PRIMARY KEY (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `menu_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
    `title`     VARCHAR(32)  NOT NULL                COMMENT '菜单标题',
    `path`      VARCHAR(64)  DEFAULT NULL            COMMENT '路由路径',
    `icon`      VARCHAR(32)  DEFAULT NULL            COMMENT '图标名称',
    `parent_id` BIGINT       DEFAULT NULL            COMMENT '父菜单ID',
    `user_type` VARCHAR(16)  NOT NULL                COMMENT '可见用户类型',
    PRIMARY KEY (`menu_id`)
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
    `amount`       DECIMAL(10,2) NOT NULL               COMMENT '支付金额',
    `payment_type` VARCHAR(16)  DEFAULT '学费'          COMMENT '类型：学费/一卡通充值/消费',
    `description`  VARCHAR(128) DEFAULT NULL            COMMENT '描述',
    `payment_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    PRIMARY KEY (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 固定资产表
CREATE TABLE IF NOT EXISTS `asset` (
    `asset_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资产主键ID',
    `asset_name`   VARCHAR(64)  NOT NULL                COMMENT '资产名称',
    `asset_type`   VARCHAR(16)  NOT NULL                COMMENT '类型：设备/办公用品/其他',
    `quantity`     INT          DEFAULT 1               COMMENT '数量',
    `dept_id`      BIGINT       DEFAULT NULL            COMMENT '所属部门ID',
    `user_id`      BIGINT       DEFAULT NULL            COMMENT '领用人ID',
    `status`       INT          DEFAULT 1               COMMENT '状态：1=在库, 2=已领用, 3=报废',
    `apply_user_id` BIGINT      DEFAULT NULL            COMMENT '申请人ID',
    `approve_status` INT        DEFAULT 0               COMMENT '审批：0=待审批, 1=已通过, 2=已拒绝',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定资产表';

-- 工作计划表
CREATE TABLE IF NOT EXISTS `work_plan` (
    `plan_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '计划主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '教职工ID',
    `plan_type`   VARCHAR(16)  NOT NULL                COMMENT '类型：周计划/月计划',
    `content`     TEXT         NOT NULL                COMMENT '计划内容',
    `start_date`  DATE         DEFAULT NULL            COMMENT '开始日期',
    `end_date`    DATE         DEFAULT NULL            COMMENT '结束日期',
    `status`      INT          DEFAULT 1               COMMENT '状态：1=进行中, 2=已完成',
    `supervisor_comment` TEXT  DEFAULT NULL            COMMENT '上级点评',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作计划表';

-- 公文表
CREATE TABLE IF NOT EXISTS `document` (
    `doc_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公文主键ID',
    `title`       VARCHAR(128) NOT NULL                COMMENT '公文标题',
    `doc_type`    VARCHAR(16)  NOT NULL                COMMENT '类型：会签/请示/请假/报告',
    `content`     TEXT         NOT NULL                COMMENT '公文内容',
    `initiator_id` BIGINT      NOT NULL                COMMENT '发起人ID',
    `current_approver_id` BIGINT DEFAULT NULL          COMMENT '当前审批人ID',
    `status`      INT          DEFAULT 0               COMMENT '状态：0=审批中, 1=已通过, 2=已拒绝, 3=已退回',
    `approval_chain` TEXT      DEFAULT NULL            COMMENT '审批链JSON',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文表';

-- 公文审批记录表
CREATE TABLE IF NOT EXISTS `document_approval` (
    `approval_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审批记录主键ID',
    `doc_id`      BIGINT       NOT NULL                COMMENT '公文ID',
    `approver_id` BIGINT       NOT NULL                COMMENT '审批人ID',
    `action`      VARCHAR(8)   NOT NULL                COMMENT '操作：同意/拒绝/退回',
    `opinion`     VARCHAR(256) DEFAULT NULL            COMMENT '审批意见',
    `approval_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批记录表';

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
-- 测试种子数据 v2
-- =====================================
DELETE FROM score WHERE 1=1; DELETE FROM course_selection WHERE 1=1;
DELETE FROM schedule WHERE 1=1; DELETE FROM course_capacity WHERE 1=1;
DELETE FROM course WHERE 1=1; DELETE FROM classroom WHERE 1=1;
DELETE FROM student WHERE 1=1;
DELETE FROM user WHERE username REGEXP '^(202[34]|00000|1000|2000)';

-- 教务处5人(工号00000001-00000005)
INSERT INTO user (username,password,user_type,status) VALUES
('00000001','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',4,1),
('00000002','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',4,1),
('00000003','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',4,1),
('00000004','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',4,1),
('00000005','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',4,1);
-- 教职工5人(工号10000001-10000005)
INSERT INTO user (username,password,user_type,status) VALUES
('10000001','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',3,1),
('10000002','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',3,1),
('10000003','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',3,1),
('10000004','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',3,1),
('10000005','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',3,1);
-- 辅导员3人(工号20000001-20000003)
INSERT INTO user (username,password,user_type,status) VALUES
('20000001','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',2,1),
('20000002','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',2,1),
('20000003','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',2,1);
-- 学生30人(2023级15+2024级15, 学号=username)
INSERT INTO user (username,password,user_type,status) VALUES
('20230001','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230002','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230003','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230004','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230005','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230006','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230007','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230008','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230009','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230010','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230011','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230012','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230013','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230014','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20230015','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240001','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240002','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240003','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240004','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240005','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240006','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240007','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240008','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240009','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240010','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240011','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240012','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240013','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240014','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1),
('20240015','$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy',1,1);

INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id FROM user u JOIN role r ON
  (u.username LIKE '2023%' AND r.role_code='STUDENT') OR
  (u.username LIKE '2024%' AND r.role_code='STUDENT') OR
  (u.username LIKE '1000%' AND r.role_code='STAFF') OR
  (u.username LIKE '2000%' AND r.role_code='COUNSELOR') OR
  (u.username LIKE '00000%' AND r.role_code='ADMIN');

INSERT INTO student (student_id, student_name, student_no, grade_id, student_age)
SELECT u.user_id, ELT(1+MOD(u.user_id,15),'张伟','李娜','王强','赵敏','陈静','刘洋','周杰','吴芳','孙鹏','郑丽','马超','黄婷','许磊','林峰','何雪'),
  u.username, IF(u.username LIKE '2023%',1,2), 18+MOD(u.user_id,4)
FROM user u WHERE u.username LIKE '2023%' OR u.username LIKE '2024%';

-- 100间教室 A/B/C/D栋各20间(大10+小10) E实验楼20间实验室
INSERT INTO classroom (classroom_id,classroom_name,building,capacity,type,status) VALUES
(1,'A101','A栋',120,'大教室',1),(2,'A102','A栋',110,'大教室',1),(3,'A103','A栋',130,'大教室',1),
(4,'A104','A栋',100,'大教室',1),(5,'A105','A栋',120,'大教室',1),(6,'A106','A栋',110,'大教室',1),
(7,'A107','A栋',130,'大教室',1),(8,'A108','A栋',100,'大教室',1),(9,'A109','A栋',120,'大教室',1),
(10,'A110','A栋',110,'大教室',1),(11,'A111','A栋',50,'小教室',1),(12,'A112','A栋',45,'小教室',1),
(13,'A113','A栋',55,'小教室',1),(14,'A114','A栋',50,'小教室',1),(15,'A115','A栋',45,'小教室',1),
(16,'A116','A栋',55,'小教室',1),(17,'A117','A栋',50,'小教室',1),(18,'A118','A栋',45,'小教室',1),
(19,'A119','A栋',55,'小教室',1),(20,'A120','A栋',50,'小教室',1),
(21,'B101','B栋',120,'大教室',1),(22,'B102','B栋',110,'大教室',1),(23,'B103','B栋',130,'大教室',1),
(24,'B104','B栋',100,'大教室',1),(25,'B105','B栋',120,'大教室',1),(26,'B106','B栋',110,'大教室',1),
(27,'B107','B栋',130,'大教室',1),(28,'B108','B栋',100,'大教室',1),(29,'B109','B栋',120,'大教室',1),
(30,'B110','B栋',110,'大教室',1),(31,'B111','B栋',50,'小教室',1),(32,'B112','B栋',45,'小教室',1),
(33,'B113','B栋',55,'小教室',1),(34,'B114','B栋',50,'小教室',1),(35,'B115','B栋',45,'小教室',1),
(36,'B116','B栋',55,'小教室',1),(37,'B117','B栋',50,'小教室',1),(38,'B118','B栋',45,'小教室',1),
(39,'B119','B栋',55,'小教室',1),(40,'B120','B栋',50,'小教室',1),
(41,'C101','C栋',120,'大教室',1),(42,'C102','C栋',110,'大教室',1),(43,'C103','C栋',130,'大教室',1),
(44,'C104','C栋',100,'大教室',1),(45,'C105','C栋',120,'大教室',1),(46,'C106','C栋',110,'大教室',1),
(47,'C107','C栋',130,'大教室',1),(48,'C108','C栋',100,'大教室',1),(49,'C109','C栋',120,'大教室',1),
(50,'C110','C栋',110,'大教室',1),(51,'C111','C栋',50,'小教室',1),(52,'C112','C栋',45,'小教室',1),
(53,'C113','C栋',55,'小教室',1),(54,'C114','C栋',50,'小教室',1),(55,'C115','C栋',45,'小教室',1),
(56,'C116','C栋',55,'小教室',1),(57,'C117','C栋',50,'小教室',1),(58,'C118','C栋',45,'小教室',1),
(59,'C119','C栋',55,'小教室',1),(60,'C120','C栋',50,'小教室',1),
(61,'D101','D栋',120,'大教室',1),(62,'D102','D栋',110,'大教室',1),(63,'D103','D栋',130,'大教室',1),
(64,'D104','D栋',100,'大教室',1),(65,'D105','D栋',120,'大教室',1),(66,'D106','D栋',110,'大教室',1),
(67,'D107','D栋',130,'大教室',1),(68,'D108','D栋',100,'大教室',1),(69,'D109','D栋',120,'大教室',1),
(70,'D110','D栋',110,'大教室',1),(71,'D111','D栋',50,'小教室',1),(72,'D112','D栋',45,'小教室',1),
(73,'D113','D栋',55,'小教室',1),(74,'D114','D栋',50,'小教室',1),(75,'D115','D栋',45,'小教室',1),
(76,'D116','D栋',55,'小教室',1),(77,'D117','D栋',50,'小教室',1),(78,'D118','D栋',45,'小教室',1),
(79,'D119','D栋',55,'小教室',1),(80,'D120','D栋',50,'小教室',1),
(81,'E101','实验楼E',30,'实验室',1),(82,'E102','实验楼E',25,'实验室',1),(83,'E103','实验楼E',30,'实验室',1),
(84,'E104','实验楼E',25,'实验室',1),(85,'E105','实验楼E',30,'实验室',1),(86,'E106','实验楼E',25,'实验室',1),
(87,'E107','实验楼E',30,'实验室',1),(88,'E108','实验楼E',25,'实验室',1),(89,'E109','实验楼E',30,'实验室',1),
(90,'E110','实验楼E',25,'实验室',1),(91,'E111','实验楼E',30,'实验室',1),(92,'E112','实验楼E',25,'实验室',1),
(93,'E113','实验楼E',30,'实验室',1),(94,'E114','实验楼E',25,'实验室',1),(95,'E115','实验楼E',30,'实验室',1),
(96,'E116','实验楼E',25,'实验室',1),(97,'E117','实验楼E',30,'实验室',1),(98,'E118','实验楼E',25,'实验室',1),
(99,'E119','实验楼E',30,'实验室',1),(100,'E120','实验楼E',25,'实验室',1);

-- 10门课程
INSERT INTO course (course_id, course_name, course_code, classification, credit, weekly_frequency, is_active) VALUES
(1,'高等数学(上)','MATH101','必修',5,2,1),(2,'线性代数','MATH102','必修',3,2,1),
(3,'大学物理','PHYS101','必修',4,2,1),(4,'大学英语(三)','ENGL101','必修',3,2,1),
(5,'数据结构与算法','CS201','必修',4,2,1),(6,'操作系统','CS301','必修',3,1,1),
(7,'马克思主义原理','POLI101','必修',2,1,1),(8,'大学体育(三)','PE101','必修',1,1,1),
(9,'Python程序设计','CS105','选修',2,1,1),(10,'数据库原理与应用','CS202','限选',3,2,1)
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

INSERT INTO course_capacity (course_id, semester, max_capacity, current_count, min_capacity) VALUES
(1,'2025-2026-1',120,30,15),(2,'2025-2026-1',100,30,15),(3,'2025-2026-1',110,30,15),
(4,'2025-2026-1',90,30,15),(5,'2025-2026-1',80,30,15),(6,'2025-2026-1',70,30,10),
(7,'2025-2026-1',150,30,20),(8,'2025-2026-1',60,30,10),(9,'2025-2026-1',100,30,15),(10,'2025-2026-1',80,30,10)
ON DUPLICATE KEY UPDATE current_count=VALUES(current_count);

-- 排课 (teacher_id用教职工user_id)
INSERT INTO schedule (course_id,classroom_id,teacher_id,semester,week_day,start_period,end_period,start_week,end_week,schedule_type,week_pattern) VALUES
(1,1,(SELECT user_id FROM user WHERE username='10000001'),'2025-2026-1',1,1,2,1,16,'正常','every'),
(1,1,(SELECT user_id FROM user WHERE username='10000001'),'2025-2026-1',3,3,5,1,16,'正常','every'),
(2,21,(SELECT user_id FROM user WHERE username='10000002'),'2025-2026-1',2,3,5,1,16,'正常','every'),
(3,41,(SELECT user_id FROM user WHERE username='10000003'),'2025-2026-1',3,8,10,1,16,'正常','every'),
(4,11,(SELECT user_id FROM user WHERE username='10000004'),'2025-2026-1',4,3,5,1,16,'正常','every'),
(5,61,(SELECT user_id FROM user WHERE username='10000005'),'2025-2026-1',5,8,10,1,16,'正常','every'),
(6,21,(SELECT user_id FROM user WHERE username='10000001'),'2025-2026-1',1,8,10,1,16,'正常','every'),
(7,61,(SELECT user_id FROM user WHERE username='10000002'),'2025-2026-1',2,1,2,1,16,'正常','every'),
(8,81,(SELECT user_id FROM user WHERE username='10000003'),'2025-2026-1',5,6,7,1,16,'正常','every'),
(9,31,(SELECT user_id FROM user WHERE username='10000004'),'2025-2026-1',3,1,2,1,16,'正常','every'),
(10,11,(SELECT user_id FROM user WHERE username='10000005'),'2025-2026-1',4,8,10,1,16,'正常','every');

-- 选课 (每个学生选3-5门课, 用子查询获取实际schedule_id)
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 1, (SELECT schedule_id FROM schedule WHERE course_id=1 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2023%' LIMIT 12;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 2, (SELECT schedule_id FROM schedule WHERE course_id=2 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2023%' LIMIT 10;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 3, (SELECT schedule_id FROM schedule WHERE course_id=3 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2023%' LIMIT 8;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 4, (SELECT schedule_id FROM schedule WHERE course_id=4 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2023%' LIMIT 10;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 7, (SELECT schedule_id FROM schedule WHERE course_id=7 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2023%' LIMIT 12;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 5, (SELECT schedule_id FROM schedule WHERE course_id=5 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2024%' LIMIT 10;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 6, (SELECT schedule_id FROM schedule WHERE course_id=6 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2024%' LIMIT 8;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 8, (SELECT schedule_id FROM schedule WHERE course_id=8 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2024%' LIMIT 10;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 9, (SELECT schedule_id FROM schedule WHERE course_id=9 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2024%' LIMIT 10;
INSERT INTO course_selection (student_id,course_id,schedule_id,semester,status,select_time)
SELECT u.user_id, 10, (SELECT schedule_id FROM schedule WHERE course_id=10 AND semester='2025-2026-1' LIMIT 1), '2025-2026-1', 1, NOW() FROM user u WHERE u.username LIKE '2024%' LIMIT 8;

-- 成绩 (SELECT来自有选课的学生)
INSERT INTO score (student_id,course_id,score_score,semester,gpa,status)
SELECT cs.student_id, cs.course_id,
  60+FLOOR(RAND()*41), '2025-2026-1',
  ELT(1+FLOOR(RAND()*5),1.0,1.5,2.0,3.0,4.0), 1
FROM course_selection cs WHERE cs.semester='2025-2026-1' AND cs.student_id % 3 <> 0;

INSERT INTO score (student_id,course_id,score_score,semester,gpa,status)
SELECT cs.student_id, cs.course_id,
  30+FLOOR(RAND()*29), '2025-2026-1', 0.0, 0
FROM course_selection cs WHERE cs.semester='2025-2026-1' AND cs.student_id % 3 = 0;




