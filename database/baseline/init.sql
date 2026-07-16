-- ============================================
-- 智慧校园服务平台 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS school_spring
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE school_spring;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    `username`  VARCHAR(32)  NOT NULL                COMMENT '用户名/账号',
    `password`  VARCHAR(32)  NOT NULL                COMMENT '密码',
    `user_type` INT          NOT NULL DEFAULT 1      COMMENT '用户类型: 1=学生, 2=老师',
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

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `score_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '成绩主键ID',
    `student_id`  BIGINT NOT NULL                COMMENT '学生ID',
    `score_score` INT    DEFAULT NULL            COMMENT '成绩分数',
    `course_id`   BIGINT NOT NULL                COMMENT '课程ID',
    PRIMARY KEY (`score_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `menu_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
    `title`     VARCHAR(32)  NOT NULL                COMMENT '菜单标题',
    `path`      VARCHAR(64)  DEFAULT NULL            COMMENT '路由路径',
    `icon`      VARCHAR(32)  DEFAULT NULL            COMMENT '图标名称',
    `parent_id` BIGINT       DEFAULT NULL            COMMENT '父菜单ID',
    `user_type` VARCHAR(16)  NOT NULL                COMMENT '可见用户类型: 1,2 或 1,2',
    PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
