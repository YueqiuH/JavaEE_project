package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.ForumComment;
import com.smartcampus.contract.vo.ForumCommentVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    @Select("""
            SELECT c.comment_id, c.post_id, c.author_id,
                   COALESCE(u.real_name, u.username) AS author_name,
                   c.content, c.status, c.create_time
            FROM forum_comment c
            LEFT JOIN `user` u ON u.user_id = c.author_id
            WHERE c.post_id = #{postId} AND c.status = 1
            ORDER BY c.create_time
            """)
    List<ForumCommentVo> selectByPostId(@Param("postId") Long postId);
}
