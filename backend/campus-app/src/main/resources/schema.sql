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

-- 容量
INSERT INTO course_capacity (course_id, semester, max_capacity, current_count, min_capacity) VALUES
(1,'2025-2026-1',60,50,15),(2,'2025-2026-1',50,45,15),
(3,'2025-2026-1',55,48,15),(4,'2025-2026-1',45,42,15),
(5,'2025-2026-1',40,38,15),(6,'2025-2026-1',35,30,10),
(7,'2025-2026-1',80,78,20),(8,'2025-2026-1',30,28,10),
(9,'2025-2026-1',60,55,15),(10,'2025-2026-1',40,35,10)
ON DUPLICATE KEY UPDATE current_count=VALUES(current_count);

-- 排课（teacherId: 3=700001, 4=800001, 5=admin, 后续=teacher01/02）
INSERT INTO schedule (course_id, classroom_id, teacher_id, semester, week_day, start_period, end_period, start_week, end_week, schedule_type, week_pattern) VALUES
(1, 6, 3, '2025-2026-1', 1, 1, 2, 1, 16, '正常', 'every'),
(1, 6, 3, '2025-2026-1', 3, 3, 5, 1, 16, '正常', 'every'),
(2, 1, 3, '2025-2026-1', 2, 3, 5, 1, 16, '正常', 'every'),
(3, 3, 3, '2025-2026-1', 3, 8, 10, 1, 16, '正常', 'every'),
(4, 2, 4, '2025-2026-1', 4, 3, 5, 1, 16, '正常', 'every'),
(5, 3, 4, '2025-2026-1', 5, 8, 10, 1, 16, '正常', 'every'),
(6, 1, 3, '2025-2026-1', 1, 8, 10, 1, 16, '正常', 'every'),
(7, 6, 4, '2025-2026-1', 2, 1, 2, 1, 16, '正常', 'every'),
(8, 4, 5, '2025-2026-1', 5, 6, 7, 1, 16, '正常', 'every'),
(9, 5, 4, '2025-2026-1', 3, 1, 2, 1, 16, '正常', 'every'),
(10,1, 3, '2025-2026-1', 4, 8, 10, 1, 16, '正常', 'every')
ON DUPLICATE KEY UPDATE week_day=VALUES(week_day);

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
ON DUPLICATE KEY UPDATE score_score=VALUES(score_score);


