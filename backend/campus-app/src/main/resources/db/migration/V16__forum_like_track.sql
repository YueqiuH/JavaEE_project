-- ============================================
-- 鎴愬憳 D锛氳鍧涚偣璧炲幓閲?鈥?姣忎汉姣忓笘鍙兘璧炰竴娆?
-- ============================================

CREATE TABLE IF NOT EXISTS `forum_post_like` (
    `id`      BIGINT   NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT   NOT NULL COMMENT '甯栧瓙ID',
    `user_id` BIGINT   NOT NULL COMMENT '鐐硅禐鐢ㄦ埛ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯栧瓙鐐硅禐璁板綍';

