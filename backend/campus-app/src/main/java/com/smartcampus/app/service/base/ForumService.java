package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.ForumCommentCreateRequest;
import com.smartcampus.contract.dto.ForumPostCreateRequest;
import com.smartcampus.contract.entity.ForumPost;
import com.smartcampus.contract.vo.ForumCommentVo;
import com.smartcampus.contract.vo.ForumPostVo;

import java.util.List;

public interface ForumService {

    /** 分页查询帖子；无 base:write 权限时只能查看正常状态的帖子 */
    IPage<ForumPostVo> pagePosts(PageParam pageParam, String keyword, Integer status);

    /** 查看帖子详情并累加浏览数 */
    ForumPostVo getPost(Long postId);

    ForumPost createPost(ForumPostCreateRequest request);

    /** 点赞（简单计数） */
    void likePost(Long postId);

    /** 作者本人或管理员删除帖子（软删除） */
    void deletePost(Long postId);

    /** 管理员封禁(-1)/恢复(1)帖子 */
    void moderatePost(Long postId, Integer status);

    List<ForumCommentVo> listComments(Long postId);

    ForumCommentVo addComment(Long postId, ForumCommentCreateRequest request);

    /** 作者本人或管理员删除回复（软删除） */
    void deleteComment(Long commentId);
}
