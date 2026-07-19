package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.entity.ForumPost;
import com.smartcampus.contract.vo.ForumPostVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ForumPostMapper extends BaseMapper<ForumPost> {

    @Select("""
            SELECT p.post_id, p.title, p.content, p.author_id,
                   COALESCE(u.real_name, u.username) AS author_name,
                   p.like_count, p.view_count, p.status, p.create_time,
                   COUNT(c.comment_id) AS comment_count
            FROM forum_post p
            LEFT JOIN `user` u ON u.user_id = p.author_id
            LEFT JOIN forum_comment c ON c.post_id = p.post_id AND c.status = 1
            WHERE (#{status} IS NULL OR p.status = #{status})
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR p.title LIKE CONCAT('%', #{keyword}, '%')
                   OR p.content LIKE CONCAT('%', #{keyword}, '%'))
            GROUP BY p.post_id, p.title, p.content, p.author_id, author_name,
                     p.like_count, p.view_count, p.status, p.create_time
            ORDER BY p.create_time DESC
            """)
    IPage<ForumPostVo> selectVoPage(IPage<ForumPostVo> page,
                                    @Param("status") Integer status,
                                    @Param("keyword") String keyword);

    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE post_id = #{postId}")
    int increaseViewCount(@Param("postId") Long postId);

    @Update("UPDATE forum_post SET like_count = like_count + 1 WHERE post_id = #{postId}")
    int increaseLikeCount(@Param("postId") Long postId);

    @Update("UPDATE forum_post SET like_count = GREATEST(like_count - 1, 0) WHERE post_id = #{postId}")
    int decreaseLikeCount(@Param("postId") Long postId);

    @Select("""
            SELECT p.post_id, p.title, p.content, p.author_id,
                   COALESCE(u.real_name, u.username) AS author_name,
                   p.like_count, p.view_count, p.status, p.create_time,
                   COUNT(c.comment_id) AS comment_count
            FROM forum_post p
            LEFT JOIN `user` u ON u.user_id = p.author_id
            LEFT JOIN forum_comment c ON c.post_id = p.post_id AND c.status = 1
            WHERE p.post_id = #{postId}
            GROUP BY p.post_id, p.title, p.content, p.author_id, author_name,
                     p.like_count, p.view_count, p.status, p.create_time
            """)
    ForumPostVo selectVoById(@Param("postId") Long postId);
}
