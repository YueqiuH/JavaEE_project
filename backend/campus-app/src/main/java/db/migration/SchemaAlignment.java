package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

final class SchemaAlignment {

    private static final String DEMO_PASSWORD_HASH =
            "$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy";

    private final Connection connection;
    private final String schema;

    private SchemaAlignment(Connection connection) throws SQLException {
        this.connection = connection;
        this.schema = connection.getCatalog();
    }

    static void prepareLegacySchema(Connection connection) throws SQLException {
        SchemaAlignment migration = new SchemaAlignment(connection);
        migration.addColumn("menu", "permission_code", "VARCHAR(64) DEFAULT NULL");
        migration.addColumn("menu", "sort_order", "INT NOT NULL DEFAULT 0");
        migration.addColumn("menu", "status", "TINYINT NOT NULL DEFAULT 1");

        migration.addColumn("document", "workflow_id", "BIGINT DEFAULT NULL");
        migration.addColumn("document", "current_step", "INT DEFAULT NULL");
        migration.addColumn("document", "approval_round", "INT NOT NULL DEFAULT 1");

        migration.addColumn("document_approval", "task_id", "BIGINT DEFAULT NULL");
        migration.addColumn("document_approval", "round_no", "INT DEFAULT NULL");
        migration.addColumn("document_approval", "step_order", "INT DEFAULT NULL");
        migration.addColumn("document_approval", "step_name", "VARCHAR(64) DEFAULT NULL");
    }

    static void alignFinalSchema(Connection connection) throws SQLException {
        SchemaAlignment migration = new SchemaAlignment(connection);
        migration.alignBaseData();
        migration.alignTeaching();
        migration.alignStudentAffairs();
        migration.alignOffice();
        migration.seedRbacAndDemoData();
    }

    static void alignFinalPermissions(Connection connection) throws SQLException {
        new SchemaAlignment(connection).seedRolePermissions();
    }

    private void alignBaseData() throws SQLException {
        addColumn("user", "real_name", "VARCHAR(32) DEFAULT NULL");
        addColumn("user", "gender", "TINYINT DEFAULT NULL");
        addColumn("user", "phone", "VARCHAR(20) DEFAULT NULL");
        addColumn("user", "email", "VARCHAR(64) DEFAULT NULL");
        addColumn("user", "title", "VARCHAR(32) DEFAULT NULL");
        addColumn("user", "position", "VARCHAR(32) DEFAULT NULL");
        addColumn("user", "dept_id", "BIGINT DEFAULT NULL");

        addColumn("student", "gender", "TINYINT DEFAULT NULL");
        addColumn("student", "dept_id", "BIGINT DEFAULT NULL");
        addColumn("student", "major_id", "BIGINT DEFAULT NULL");
        addColumn("student", "class_name", "VARCHAR(32) DEFAULT NULL");
        addColumn("student", "origin_place", "VARCHAR(32) DEFAULT NULL");
        addColumn("student", "enroll_year", "INT DEFAULT NULL");
        addColumn("student", "status", "TINYINT NOT NULL DEFAULT 1");

        addIndex("student", "uk_student_no", true, "student_no");
        addIndex("student", "idx_student_dept_major", false, "dept_id", "major_id");
        addIndex("department", "uk_dept_code", true, "dept_code");
        addIndex("major", "uk_major_code", true, "major_code");
        addIndex("enrollment", "uk_enrollment_major_year", true, "major_id", "year");

        execute("""
                CREATE TABLE IF NOT EXISTS `forum_post_like` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `post_id` BIGINT NOT NULL,
                    `user_id` BIGINT NOT NULL,
                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_forum_post_like` (`post_id`, `user_id`),
                    KEY `idx_forum_post_like_user` (`user_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子点赞记录'
                """);
    }

    private void alignTeaching() throws SQLException {
        addColumn("course", "course_code", "VARCHAR(16) DEFAULT NULL");
        addColumn("course", "classification", "VARCHAR(8) DEFAULT NULL");
        addColumn("course", "credit", "DECIMAL(3,1) DEFAULT NULL");
        addColumn("course", "weekly_frequency", "INT DEFAULT NULL");
        addColumn("course", "prerequisite_id", "BIGINT DEFAULT NULL");
        addColumn("course", "is_active", "TINYINT NOT NULL DEFAULT 1");
        addColumn("course", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");

        addColumn("schedule", "parent_id", "BIGINT DEFAULT NULL");
        addColumn("schedule", "week_pattern", "VARCHAR(16) NOT NULL DEFAULT 'every'");
        addColumn("schedule", "target_grade_id", "BIGINT DEFAULT NULL");
        addIndex("schedule", "idx_schedule_parent", false, "parent_id");
        addIndex("schedule", "idx_schedule_target_grade", false, "target_grade_id");

        addColumn("course_capacity", "schedule_id", "BIGINT DEFAULT NULL");
        addIndex("course_capacity", "idx_course_capacity_schedule", false, "schedule_id");

        addColumn("evaluation", "schedule_id", "BIGINT DEFAULT NULL");
        addIndex("evaluation", "uk_evaluation_student_schedule", true, "student_id", "schedule_id");
        addIndex("evaluation", "idx_evaluation_teacher_semester", false, "teacher_id", "semester");
        addIndex("evaluation", "idx_evaluation_teacher_course", false, "teacher_id", "course_id", "semester");
        addIndex("score", "uk_score_student_course_sem", true, "student_id", "course_id", "semester");
    }

    private void alignStudentAffairs() throws SQLException {
        execute("""
                CREATE TABLE IF NOT EXISTS `student_profile_extension` (
                    `student_id` BIGINT NOT NULL,
                    `current_address` VARCHAR(128) DEFAULT NULL,
                    `phone` VARCHAR(20) DEFAULT NULL,
                    `email` VARCHAR(128) DEFAULT NULL,
                    `emergency_contact` VARCHAR(32) DEFAULT NULL,
                    `emergency_phone` VARCHAR(20) DEFAULT NULL,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`student_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生非核心联系信息扩展表'
                """);

        addColumn("scholarship", "application_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("scholarship", "reviewed_at", "DATETIME DEFAULT NULL");
        addColumn("scholarship", "selected_at", "DATETIME DEFAULT NULL");
        addColumn("scholarship", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumn("scholarship", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        execute("UPDATE `scholarship` SET `application_no` = CONCAT('LEGACYJZ', LPAD(`scholarship_id`, 10, '0')) WHERE `application_no` IS NULL");
        addIndex("scholarship", "uk_scholarship_application_no", true, "application_no");
        addIndex("scholarship", "idx_scholarship_student_status", false, "student_id", "status");
        addIndex("scholarship", "idx_scholarship_status_updated", false, "status", "updated_at");

        addColumn("student_status_change", "application_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("student_status_change", "desired_effective_date", "DATE DEFAULT NULL");
        addColumn("student_status_change", "counselor_reviewed_at", "DATETIME DEFAULT NULL");
        addColumn("student_status_change", "academic_reviewer_id", "BIGINT DEFAULT NULL");
        addColumn("student_status_change", "academic_opinion", "VARCHAR(256) DEFAULT NULL");
        addColumn("student_status_change", "academic_reviewed_at", "DATETIME DEFAULT NULL");
        addColumn("student_status_change", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumn("student_status_change", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        copyColumn("student_status_change", "admin_id", "academic_reviewer_id");
        copyColumn("student_status_change", "admin_opinion", "academic_opinion");
        execute("UPDATE `student_status_change` SET `application_no` = CONCAT('LEGACYXJ', LPAD(`change_id`, 10, '0')) WHERE `application_no` IS NULL");
        execute("UPDATE `student_status_change` SET `desired_effective_date` = COALESCE(DATE(`apply_time`), CURRENT_DATE) WHERE `desired_effective_date` IS NULL");
        addIndex("student_status_change", "uk_status_change_application_no", true, "application_no");
        addIndex("student_status_change", "idx_status_change_student_status", false, "student_id", "status");
        addIndex("student_status_change", "idx_status_change_status_updated", false, "status", "updated_at");

        alignCompetition();
        alignLaboratory();
    }

    private void alignCompetition() throws SQLException {
        addColumn("competition", "competition_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("competition", "requirements", "TEXT DEFAULT NULL");
        addColumn("competition", "min_members", "INT NOT NULL DEFAULT 1");
        addColumn("competition", "max_members", "INT NOT NULL DEFAULT 5");
        addColumn("competition", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        addColumn("competition", "published_at", "DATETIME DEFAULT NULL");
        addColumn("competition", "closed_at", "DATETIME DEFAULT NULL");
        execute("UPDATE `competition` SET `competition_no` = CONCAT('LEGACYJS', LPAD(`competition_id`, 10, '0')) WHERE `competition_no` IS NULL");
        execute("UPDATE `competition` SET `requirements` = COALESCE(NULLIF(`description`, ''), '请联系竞赛发布教师确认参赛要求') WHERE `requirements` IS NULL");
        addIndex("competition", "uk_competition_no", true, "competition_no");
        addIndex("competition", "idx_competition_publisher_status", false, "publisher_id", "status");
        addIndex("competition", "idx_competition_status_deadline", false, "status", "deadline");

        addColumn("competition_team", "registration_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("competition_team", "material_url", "VARCHAR(256) DEFAULT NULL");
        addColumn("competition_team", "material_storage_name", "VARCHAR(128) DEFAULT NULL");
        addColumn("competition_team", "material_original_name", "VARCHAR(255) DEFAULT NULL");
        addColumn("competition_team", "material_content_type", "VARCHAR(128) DEFAULT NULL");
        addColumn("competition_team", "material_size", "BIGINT DEFAULT NULL");
        addColumn("competition_team", "material_description", "VARCHAR(1000) DEFAULT NULL");
        addColumn("competition_team", "review_opinion", "VARCHAR(500) DEFAULT NULL");
        addColumn("competition_team", "submitted_at", "DATETIME DEFAULT NULL");
        addColumn("competition_team", "reviewed_at", "DATETIME DEFAULT NULL");
        addColumn("competition_team", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        execute("UPDATE `competition_team` SET `registration_no` = CONCAT('LEGACYTD', LPAD(`team_id`, 10, '0')) WHERE `registration_no` IS NULL");
        addIndex("competition_team", "uk_competition_registration_no", true, "registration_no");
        addIndex("competition_team", "uk_competition_team_name", true, "competition_id", "team_name");
        addIndex("competition_team", "idx_competition_team_leader_status", false, "leader_id", "status");
        addIndex("competition_team", "idx_competition_team_competition_status", false, "competition_id", "status");

        addColumn("competition_member", "invitation_status", "INT NOT NULL DEFAULT 1");
        addColumn("competition_member", "invited_by", "BIGINT DEFAULT NULL");
        addColumn("competition_member", "invited_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumn("competition_member", "responded_at", "DATETIME DEFAULT NULL");
        addIndex("competition_member", "uk_competition_team_student", true, "team_id", "student_id");
        addIndex("competition_member", "idx_competition_member_student_status", false, "student_id", "invitation_status");
    }

    private void alignLaboratory() throws SQLException {
        addColumn("lab", "lab_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("lab", "manager_id", "BIGINT DEFAULT NULL");
        addColumn("lab", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumn("lab", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        execute("UPDATE `lab` SET `lab_no` = CONCAT('LEGACYLAB', LPAD(`lab_id`, 8, '0')) WHERE `lab_no` IS NULL");
        addIndex("lab", "uk_lab_no", true, "lab_no");
        addIndex("lab", "idx_lab_manager_status", false, "manager_id", "status");

        execute("""
                CREATE TABLE IF NOT EXISTS `lab_resource` (
                    `resource_id` BIGINT NOT NULL AUTO_INCREMENT,
                    `lab_id` BIGINT NOT NULL,
                    `resource_no` VARCHAR(32) NOT NULL,
                    `resource_name` VARCHAR(64) NOT NULL,
                    `resource_type` VARCHAR(16) NOT NULL,
                    `description` VARCHAR(500) DEFAULT NULL,
                    `status` INT NOT NULL DEFAULT 1,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`resource_id`),
                    UNIQUE KEY `uk_lab_resource_no` (`lab_id`, `resource_no`),
                    KEY `idx_lab_resource_status` (`lab_id`, `status`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室设备与工位'
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS `lab_open_slot` (
                    `slot_id` BIGINT NOT NULL AUTO_INCREMENT,
                    `lab_id` BIGINT NOT NULL,
                    `open_date` DATE NOT NULL,
                    `start_period` INT NOT NULL,
                    `end_period` INT NOT NULL,
                    `created_by` BIGINT NOT NULL,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`slot_id`),
                    KEY `idx_lab_slot_date` (`lab_id`, `open_date`, `start_period`, `end_period`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室开放时段'
                """);

        addColumn("lab_booking", "booking_no", "VARCHAR(32) DEFAULT NULL");
        addColumn("lab_booking", "resource_id", "BIGINT DEFAULT NULL");
        addColumn("lab_booking", "cancelled_at", "DATETIME DEFAULT NULL");
        addColumn("lab_booking", "check_in_at", "DATETIME DEFAULT NULL");
        addColumn("lab_booking", "check_out_at", "DATETIME DEFAULT NULL");
        addColumn("lab_booking", "expires_at", "DATETIME DEFAULT NULL");
        addColumn("lab_booking", "completed_at", "DATETIME DEFAULT NULL");
        addColumn("lab_booking", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        execute("UPDATE `lab_booking` SET `booking_no` = CONCAT('LEGACYBOOK', LPAD(`booking_id`, 8, '0')) WHERE `booking_no` IS NULL");
        execute("UPDATE `lab_booking` SET `purpose` = '历史预约' WHERE `purpose` IS NULL");
        execute("UPDATE `lab_booking` SET `expires_at` = DATE_ADD(COALESCE(`create_time`, CURRENT_TIMESTAMP), INTERVAL 30 MINUTE) WHERE `status` = 1 AND `expires_at` IS NULL");
        addIndex("lab_booking", "uk_lab_booking_no", true, "booking_no");
        addIndex("lab_booking", "idx_lab_booking_student_status", false, "student_id", "status", "booking_date");
        addIndex("lab_booking", "idx_lab_booking_resource_date", false, "resource_id", "booking_date", "status");
        addIndex("lab_booking", "idx_lab_booking_capacity", false, "lab_id", "booking_date", "status", "expires_at");

        execute("""
                CREATE TABLE IF NOT EXISTS `lab_booking_period` (
                    `booking_id` BIGINT NOT NULL,
                    `resource_id` BIGINT NOT NULL,
                    `student_id` BIGINT NOT NULL,
                    `booking_date` DATE NOT NULL,
                    `period_no` INT NOT NULL,
                    PRIMARY KEY (`booking_id`, `period_no`),
                    UNIQUE KEY `uk_booking_resource_period` (`resource_id`, `booking_date`, `period_no`),
                    UNIQUE KEY `uk_booking_student_period` (`student_id`, `booking_date`, `period_no`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约冲突占用明细'
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS `lab_booking_notice` (
                    `notice_id` BIGINT NOT NULL AUTO_INCREMENT,
                    `booking_id` BIGINT NOT NULL,
                    `student_id` BIGINT NOT NULL,
                    `title` VARCHAR(128) NOT NULL,
                    `content` VARCHAR(500) NOT NULL,
                    `is_read` INT NOT NULL DEFAULT 0,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `read_at` DATETIME DEFAULT NULL,
                    PRIMARY KEY (`notice_id`),
                    KEY `idx_lab_notice_student_read` (`student_id`, `is_read`, `created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约站内通知'
                """);
    }

    private void alignOffice() throws SQLException {
        addColumn("payment", "work_plan_id", "BIGINT DEFAULT NULL");
        addIndex("payment", "uk_payment_work_plan", true, "work_plan_id");

        addColumn("asset", "application_type", "VARCHAR(16) DEFAULT NULL");
        addColumn("asset", "source_asset_id", "BIGINT DEFAULT NULL");
        addColumn("asset", "application_reason", "VARCHAR(512) DEFAULT NULL");
        addColumn("asset", "approve_user_id", "BIGINT DEFAULT NULL");
        addColumn("asset", "approve_remark", "VARCHAR(256) DEFAULT NULL");
        addColumn("asset", "approve_time", "DATETIME DEFAULT NULL");
        addIndex("asset", "idx_asset_application", false, "application_type", "approve_status", "create_time");
        addIndex("asset", "idx_asset_source", false, "source_asset_id");

        addColumn("work_plan", "assigner_id", "BIGINT DEFAULT NULL");
        addColumn("work_plan", "wage_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0.00");
        addColumn("work_plan", "wage_paid", "TINYINT NOT NULL DEFAULT 0");
        addColumn("work_plan", "wage_paid_time", "DATETIME DEFAULT NULL");
        addIndex("work_plan", "idx_work_plan_assigner", false, "assigner_id", "status", "wage_paid");

        addColumn("document", "workflow_id", "BIGINT DEFAULT NULL");
        addColumn("document", "current_step", "INT DEFAULT NULL");
        addColumn("document", "approval_round", "INT NOT NULL DEFAULT 1");
        addColumn("document_approval", "task_id", "BIGINT DEFAULT NULL");
        addColumn("document_approval", "round_no", "INT DEFAULT NULL");
        addColumn("document_approval", "step_order", "INT DEFAULT NULL");
        addColumn("document_approval", "step_name", "VARCHAR(64) DEFAULT NULL");
    }

    private void seedRbacAndDemoData() throws SQLException {
        execute("""
                INSERT INTO `role` (`role_code`, `role_name`, `status`) VALUES
                    ('STUDENT', '学生', 1), ('TEACHER', '教师', 1),
                    ('STAFF', '教职工', 1), ('COUNSELOR', '辅导员', 1),
                    ('ADMIN', '系统管理员', 1)
                ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `status` = 1
                """);

        seedPermissions();
        seedDemoAccounts();
        seedRolePermissions();
        seedBaseDemoData();
    }

    private void seedPermissions() throws SQLException {
        execute("""
                INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
                    ('forum:read', '读取新闻与论坛', 1),
                    ('evaluation:task:read-self', '查看本人评教任务', 1),
                    ('evaluation:submit-self', '提交本人匿名评教', 1),
                    ('evaluation:result:read-self', '查看个人评教结果', 1),
                    ('competition:read', '查看学科竞赛', 1),
                    ('competition:publish', '发布学科竞赛', 1),
                    ('competition:manage-self', '维护本人发布的竞赛', 1),
                    ('competition:team:read-self', '查看本人竞赛队伍', 1),
                    ('competition:team:create', '发起竞赛组队', 1),
                    ('competition:team:manage-self', '维护本人负责的竞赛队伍', 1),
                    ('competition:team:submit-self', '提交本人竞赛报名', 1),
                    ('competition:invitation:respond-self', '处理本人组队邀请', 1),
                    ('competition:review:read-self', '查看本人竞赛的审核队列', 1),
                    ('competition:review:submit-self', '审核本人竞赛的参赛队伍', 1),
                    ('lab:read', '查看实验室与可预约资源', 1),
                    ('lab:manage-self', '维护本人负责的实验室', 1),
                    ('lab:resource:manage-self', '维护本人实验室资源', 1),
                    ('lab:slot:manage-self', '维护本人实验室开放时段', 1),
                    ('lab:booking:read-self', '查看本人实验室预约', 1),
                    ('lab:booking:create', '创建实验室预约', 1),
                    ('lab:booking:cancel-self', '取消本人实验室预约', 1),
                    ('lab:booking:read-managed', '查看本人管理实验室的预约', 1),
                    ('lab:booking:complete-managed', '完成人管理实验室的预约', 1),
                    ('lab:booking:check-in-self', '本人实验室预约签到', 1),
                    ('lab:booking:check-out-self', '本人实验室预约签退', 1),
                    ('lab:notice:read-self', '查看本人预约通知', 1),
                    ('lab:notice:mark-self', '标记本人预约通知', 1)
                ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1
                """);
    }

    private void seedDemoAccounts() throws SQLException {
        execute("""
                INSERT INTO `user` (`username`, `password`, `user_type`, `status`) VALUES
                    ('600001', '%s', 1, 1),
                    ('700001', '%s', 2, 1),
                    ('800001', '%s', 3, 1),
                    ('admin', '%s', 4, 1)
                ON DUPLICATE KEY UPDATE `password` = VALUES(`password`),
                    `user_type` = VALUES(`user_type`), `status` = 1
                """.formatted(DEMO_PASSWORD_HASH, DEMO_PASSWORD_HASH, DEMO_PASSWORD_HASH, DEMO_PASSWORD_HASH));

        execute("""
                UPDATE `user` SET `real_name` = CASE `username`
                    WHEN '600001' THEN '演示学生'
                    WHEN '700001' THEN '演示教师'
                    WHEN '800001' THEN '演示教职工'
                    WHEN 'admin' THEN '系统管理员' END
                WHERE `username` IN ('600001', '700001', '800001', 'admin')
                """);

        execute("""
                DELETE ur FROM `user_role` ur
                JOIN `user` u ON u.user_id = ur.user_id
                WHERE u.username IN ('600001', '700001', '800001', 'admin')
                """);
        execute("""
                INSERT INTO `user_role` (`user_id`, `role_id`)
                SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON
                    (u.username = '600001' AND r.role_code = 'STUDENT') OR
                    (u.username = '700001' AND r.role_code = 'TEACHER') OR
                    (u.username = '800001' AND r.role_code = 'STAFF') OR
                    (u.username = 'admin' AND r.role_code = 'ADMIN')
                """);
    }

    private void seedRolePermissions() throws SQLException {
        grant("STUDENT", "teaching:read", "student:read", "student:write",
                "fee:self:read", "fee:self:pay", "work-plan:self", "document:self",
                "notification:self:read", "scholarship:application:read-self",
                "scholarship:application:create", "scholarship:application:update-self",
                "scholarship:application:submit-self", "scholarship:application:withdraw-self",
                "status:profile:read-self", "status:profile:update-self", "status:change:read-self",
                "status:change:create", "status:change:update-self", "status:change:submit-self",
                "status:change:withdraw-self", "evaluation:task:read-self", "evaluation:submit-self",
                "competition:read", "competition:team:read-self", "competition:team:create",
                "competition:team:manage-self", "competition:team:submit-self",
                "competition:invitation:respond-self", "lab:read", "lab:booking:read-self",
                "lab:booking:create", "lab:booking:cancel-self", "lab:booking:check-in-self",
                "lab:booking:check-out-self", "lab:notice:read-self", "lab:notice:mark-self");
        grant("TEACHER", "teaching:read", "teaching:write", "student:read", "base:read",
                "office:read", "fee:overview:read", "asset:read", "asset:apply",
                "work-plan:self", "work-plan:manage", "document:self", "document:approve",
                "meeting:self", "meeting:manage", "notification:self:read",
                "scholarship:review:read", "scholarship:review:submit", "scholarship:result:generate",
                "status:review:read", "status:review:submit", "evaluation:result:read-self",
                "competition:read", "competition:publish",
                "competition:manage-self", "competition:review:read-self", "competition:review:submit-self",
                "lab:read", "lab:manage-self", "lab:resource:manage-self", "lab:slot:manage-self",
                "lab:booking:read-managed", "lab:booking:complete-managed");
        grant("STAFF", "student:read", "student:write", "base:read", "office:read", "office:write",
                "fee:self:read", "fee:self:pay", "fee:manage", "fee:overview:read",
                "asset:read", "asset:apply", "asset:manage", "work-plan:self", "work-plan:manage",
                "document:self", "document:approve", "document:manage", "meeting:self",
                "meeting:manage", "notification:self:read");

        execute("""
                INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
                SELECT r.role_id, p.permission_id FROM `role` r CROSS JOIN `permission` p
                WHERE r.role_code = 'ADMIN' OR p.permission_code = 'forum:read'
                """);
        execute("""
                DELETE rp FROM `role_permission` rp
                JOIN `role` r ON r.role_id = rp.role_id
                JOIN `permission` p ON p.permission_id = rp.permission_id
                WHERE r.role_code = 'STUDENT' AND p.permission_code = 'base:read'
                """);
    }

    private void seedBaseDemoData() throws SQLException {
        execute("INSERT INTO `grade` (`grade_name`) SELECT '2024级' WHERE NOT EXISTS (SELECT 1 FROM `grade` WHERE `grade_name` = '2024级')");
        execute("""
                INSERT INTO `department` (`dept_name`, `dept_code`, `description`)
                VALUES ('计算机学院', 'CS', '智慧校园演示院系')
                ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`)
                """);
        execute("""
                INSERT INTO `major` (`dept_id`, `major_name`, `major_code`, `cultivation_plan`)
                SELECT d.dept_id, '计算机科学与技术', 'CS01', '计算机科学与技术培养方案'
                FROM `department` d WHERE d.dept_code = 'CS'
                ON DUPLICATE KEY UPDATE `dept_id` = VALUES(`dept_id`), `major_name` = VALUES(`major_name`)
                """);
        execute("""
                UPDATE `student` s
                JOIN `department` d ON d.dept_code = 'CS'
                JOIN `major` m ON m.major_code = 'CS01'
                JOIN `grade` g ON g.grade_name = '2024级'
                SET s.student_name = '演示学生', s.gender = 1, s.dept_id = d.dept_id,
                    s.major_id = m.major_id, s.grade_id = g.grade_id, s.class_name = '计科2401',
                    s.origin_place = '北京', s.enroll_year = 2024, s.status = 1
                WHERE s.student_no = 600001
                """);
        execute("""
                UPDATE `user` u JOIN `department` d ON d.dept_code = 'CS'
                SET u.dept_id = d.dept_id WHERE u.username IN ('600001', '700001', '800001')
                """);
        execute("""
                INSERT INTO `enrollment` (`major_id`, `plan_count`, `actual_count`, `year`, `report_rate`)
                SELECT m.major_id, 60, 58, 2024, 96.67 FROM `major` m WHERE m.major_code = 'CS01'
                ON DUPLICATE KEY UPDATE `plan_count` = VALUES(`plan_count`),
                    `actual_count` = VALUES(`actual_count`), `report_rate` = VALUES(`report_rate`)
                """);
        execute("""
                INSERT INTO `news` (`title`, `content`, `news_type`, `publisher_id`, `is_pinned`)
                SELECT '智慧校园演示公告', '系统初始化完成，可使用演示账号访问各模块。', '公告', u.user_id, 1
                FROM `user` u WHERE u.username = 'admin'
                  AND NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '智慧校园演示公告')
                """);
        execute("""
                INSERT INTO `forum_post` (`title`, `content`, `author_id`, `status`)
                SELECT '智慧校园交流帖', '欢迎使用校园论坛。', u.user_id, 1
                FROM `user` u WHERE u.username = 'admin'
                  AND NOT EXISTS (SELECT 1 FROM `forum_post` WHERE `title` = '智慧校园交流帖')
                """);
    }

    private void grant(String roleCode, String... permissionCodes) throws SQLException {
        String placeholders = Arrays.stream(permissionCodes)
                .map(ignored -> "?")
                .collect(Collectors.joining(","));
        String sql = "INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`) "
                + "SELECT r.role_id, p.permission_id FROM `role` r JOIN `permission` p "
                + "ON p.permission_code IN (" + placeholders + ") WHERE r.role_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            for (String permissionCode : permissionCodes) {
                statement.setString(parameter++, permissionCode);
            }
            statement.setString(parameter, roleCode);
            statement.executeUpdate();
        }
    }

    private void copyColumn(String table, String source, String target) throws SQLException {
        if (columnExists(table, source) && columnExists(table, target)) {
            execute("UPDATE `" + table + "` SET `" + target + "` = COALESCE(`" + target + "`, `" + source + "`)");
        }
    }

    private void addColumn(String table, String column, String definition) throws SQLException {
        if (tableExists(table) && !columnExists(table, column)) {
            execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
        }
    }

    private void addIndex(String table, String index, boolean unique, String... columns) throws SQLException {
        if (!tableExists(table) || indexExists(table, index)) {
            return;
        }
        String columnList = Arrays.stream(columns)
                .map(column -> "`" + column + "`")
                .collect(Collectors.joining(", "));
        execute("ALTER TABLE `" + table + "` ADD " + (unique ? "UNIQUE " : "")
                + "KEY `" + index + "` (" + columnList + ")");
    }

    private boolean tableExists(String table) throws SQLException {
        return exists("SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                schema, table);
    }

    private boolean columnExists(String table, String column) throws SQLException {
        return exists("SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                schema, table, column);
    }

    private boolean indexExists(String table, String index) throws SQLException {
        return exists("SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?",
                schema, table, index);
    }

    private boolean exists(String sql, String... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
