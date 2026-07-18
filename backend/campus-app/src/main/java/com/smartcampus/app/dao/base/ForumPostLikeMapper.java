package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.ForumPostLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ForumPostLikeMapper extends BaseMapper<ForumPostLike> {

    @Select("SELECT COUNT(*) FROM forum_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    long countByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM forum_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
