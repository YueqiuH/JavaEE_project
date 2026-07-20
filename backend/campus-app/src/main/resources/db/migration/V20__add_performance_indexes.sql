-- ============================================
-- 添加常用 JOIN/FK 列的索引，避免全表扫描
-- ============================================

-- 教学域
CREATE INDEX IF NOT EXISTS idx_schedule_course ON `schedule`(`course_id`);
CREATE INDEX IF NOT EXISTS idx_schedule_classroom ON `schedule`(`classroom_id`);
CREATE INDEX IF NOT EXISTS idx_schedule_teacher ON `schedule`(`teacher_id`);
CREATE INDEX IF NOT EXISTS idx_course_selection_student ON `course_selection`(`student_id`);
CREATE INDEX IF NOT EXISTS idx_course_selection_schedule ON `course_selection`(`schedule_id`);
CREATE INDEX IF NOT EXISTS idx_course_capacity_course ON `course_capacity`(`course_id`);
CREATE INDEX IF NOT EXISTS idx_score_course ON `score`(`course_id`);
CREATE INDEX IF NOT EXISTS idx_exam_course ON `exam`(`course_id`);
CREATE INDEX IF NOT EXISTS idx_exam_room_exam ON `exam_room`(`exam_id`);
CREATE INDEX IF NOT EXISTS idx_exam_room_classroom ON `exam_room`(`classroom_id`);
CREATE INDEX IF NOT EXISTS idx_exam_room_student ON `exam_room`(`student_id`);
CREATE INDEX IF NOT EXISTS idx_invigilation_exam ON `invigilation`(`exam_id`);
CREATE INDEX IF NOT EXISTS idx_invigilation_teacher ON `invigilation`(`teacher_id`);

-- 选课/考试
CREATE INDEX IF NOT EXISTS idx_resit_apply_student ON `resit_apply`(`student_id`);
CREATE INDEX IF NOT EXISTS idx_resit_apply_course ON `resit_apply`(`course_id`);

-- 学生域
CREATE INDEX IF NOT EXISTS idx_student_grade ON `student`(`grade_id`);
CREATE INDEX IF NOT EXISTS idx_student_dept ON `student`(`dept_id`);
CREATE INDEX IF NOT EXISTS idx_student_major ON `student`(`major_id`);
CREATE INDEX IF NOT EXISTS idx_student_status ON `student`(`status`);
CREATE INDEX IF NOT EXISTS idx_major_dept ON `major`(`dept_id`);

-- 竞赛
CREATE INDEX IF NOT EXISTS idx_competition_publisher ON `competition`(`publisher_id`);
CREATE INDEX IF NOT EXISTS idx_competition_team_comp ON `competition_team`(`competition_id`);
CREATE INDEX IF NOT EXISTS idx_competition_team_leader ON `competition_team`(`leader_id`);
CREATE INDEX IF NOT EXISTS idx_competition_member_team ON `competition_member`(`team_id`);
CREATE INDEX IF NOT EXISTS idx_competition_member_student ON `competition_member`(`student_id`);

-- 实验室
CREATE INDEX IF NOT EXISTS idx_lab_booking_lab ON `lab_booking`(`lab_id`);
CREATE INDEX IF NOT EXISTS idx_lab_booking_student ON `lab_booking`(`student_id`);

-- 财务
CREATE INDEX IF NOT EXISTS idx_fee_student ON `fee`(`student_id`);
CREATE INDEX IF NOT EXISTS idx_payment_student ON `payment`(`student_id`);
CREATE INDEX IF NOT EXISTS idx_payment_fee ON `payment`(`fee_id`);

-- 资产
CREATE INDEX IF NOT EXISTS idx_asset_dept ON `asset`(`dept_id`);
CREATE INDEX IF NOT EXISTS idx_asset_user ON `asset`(`user_id`);

-- 计划
CREATE INDEX IF NOT EXISTS idx_work_plan_user ON `work_plan`(`user_id`);

-- 公文
CREATE INDEX IF NOT EXISTS idx_document_initiator ON `document`(`initiator_id`);
CREATE INDEX IF NOT EXISTS idx_document_approval_doc ON `document_approval`(`doc_id`);

-- 会议
CREATE INDEX IF NOT EXISTS idx_meeting_initiator ON `meeting`(`initiator_id`);
CREATE INDEX IF NOT EXISTS idx_meeting_attendee_meeting ON `meeting_attendee`(`meeting_id`);
CREATE INDEX IF NOT EXISTS idx_meeting_attendee_user ON `meeting_attendee`(`user_id`);

-- 通知
CREATE INDEX IF NOT EXISTS idx_notification_user ON `notification`(`user_id`);
CREATE INDEX IF NOT EXISTS idx_notification_read ON `notification`(`user_id`, `is_read`);

-- 内容
CREATE INDEX IF NOT EXISTS idx_news_publisher ON `news`(`publisher_id`);
CREATE INDEX IF NOT EXISTS idx_forum_post_author ON `forum_post`(`author_id`);
CREATE INDEX IF NOT EXISTS idx_forum_post_status ON `forum_post`(`status`);
CREATE INDEX IF NOT EXISTS idx_forum_comment_post ON `forum_comment`(`post_id`);
CREATE INDEX IF NOT EXISTS idx_forum_comment_author ON `forum_comment`(`author_id`);
