-- ============================================
-- 成员 D：论坛点赞去重 — 每人每帖只能赞一次
-- ============================================

CREATE TABLE IF NOT EXISTS `forum_post_like` (
    `id`      BIGINT   NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT   NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT   NOT NULL COMMENT '点赞用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录';
