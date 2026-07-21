package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.ForumCommentMapper;
import com.smartcampus.app.dao.base.ForumPostLikeMapper;
import com.smartcampus.app.dao.base.ForumPostMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.ForumService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.ForumCommentCreateRequest;
import com.smartcampus.contract.dto.ForumPostCreateRequest;
import com.smartcampus.contract.entity.ForumComment;
import com.smartcampus.contract.entity.ForumPost;
import com.smartcampus.contract.entity.ForumPostLike;
import com.smartcampus.contract.entity.User;
import com.smartcampus.contract.vo.ForumCommentVo;
import com.smartcampus.contract.vo.ForumPostVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ForumServiceImpl implements ForumService {

    /** 帖子/回复状态 */
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;
    private static final int STATUS_BANNED = -1;

    private static final String MODERATE_PERMISSION = "base:write";

    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final AuthUserMapper authUserMapper;
    private final ForumPostLikeMapper likeMapper;

    public ForumServiceImpl(ForumPostMapper postMapper, ForumCommentMapper commentMapper,
                            AuthUserMapper authUserMapper, ForumPostLikeMapper likeMapper) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.authUserMapper = authUserMapper;
        this.likeMapper = likeMapper;
    }

    @Override
    public IPage<ForumPostVo> pagePosts(PageParam pageParam, String keyword, Integer status) {
        // 无管理权限的用户只能查看正常状态的帖子
        Integer effectiveStatus = canModerate() ? status : Integer.valueOf(STATUS_NORMAL);
        Page<ForumPostVo> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        return postMapper.selectVoPage(page, effectiveStatus, keyword);
    }

    @Override
    public ForumPostVo getPost(Long postId) {
        ForumPostVo vo = postMapper.selectVoById(postId);
        if (vo == null) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_FOUND);
        }
        if (vo.getStatus() != STATUS_NORMAL && !canModerate()) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_FOUND);
        }
        postMapper.increaseViewCount(postId);
        vo.setViewCount(vo.getViewCount() + 1);
        return vo;
    }

    @Override
    public ForumPost createPost(ForumPostCreateRequest request) {
        ForumPost post = new ForumPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(CurrentUserContext.require().userId());
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setCreateTime(new Date());
        post.setStatus(STATUS_NORMAL);
        postMapper.insert(post);
        return post;
    }

    @Override
    public boolean likePost(Long postId) {
        ForumPost post = requirePost(postId);
        if (post.getStatus() != STATUS_NORMAL) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_ACTIVE);
        }
        Long userId = CurrentUserContext.require().userId();
        if (likeMapper.countByPostAndUser(postId, userId) > 0) {
            // 已点赞 → 取消点赞
            likeMapper.deleteByPostAndUser(postId, userId);
            postMapper.decreaseLikeCount(postId);
            return false;
        }
        ForumPostLike like = new ForumPostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        likeMapper.insert(like);
        postMapper.increaseLikeCount(postId);
        return true;
    }

    @Override
    public void deletePost(Long postId) {
        ForumPost post = requirePost(postId);
        if (post.getStatus() == STATUS_DELETED) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_FOUND);
        }
        assertOwnerOrModerator(post.getAuthorId());
        post.setStatus(STATUS_DELETED);
        postMapper.updateById(post);
    }

    @Override
    public void moderatePost(Long postId, Integer status) {
        if (!canModerate()) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        if (status == null || (status != STATUS_NORMAL && status != STATUS_DELETED && status != STATUS_BANNED)) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        ForumPost post = requirePost(postId);
        post.setStatus(status);
        postMapper.updateById(post);
    }

    @Override
    public List<ForumCommentVo> listComments(Long postId) {
        ForumPost post = requirePost(postId);
        if (post.getStatus() != STATUS_NORMAL && !canModerate()) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_FOUND);
        }
        return commentMapper.selectByPostId(postId);
    }

    @Override
    public ForumCommentVo addComment(Long postId, ForumCommentCreateRequest request) {
        requireActivePost(postId);
        AuthSession session = CurrentUserContext.require();
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setAuthorId(session.userId());
        comment.setContent(request.getContent());
        comment.setStatus(STATUS_NORMAL);
        comment.setCreateTime(new Date());
        commentMapper.insert(comment);

        // 查询用户真实姓名，与 listComments 的 COALESCE(u.real_name, u.username) 保持一致
        User user = authUserMapper.selectById(session.userId());
        String authorName = (user != null && user.getRealName() != null && !user.getRealName().isBlank())
                ? user.getRealName() : session.username();

        ForumCommentVo vo = new ForumCommentVo();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(postId);
        vo.setAuthorId(session.userId());
        vo.setAuthorName(authorName);
        vo.setContent(comment.getContent());
        vo.setStatus(STATUS_NORMAL);
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }

    @Override
    public void deleteComment(Long commentId) {
        ForumComment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != STATUS_NORMAL) {
            throw new BusinessException(BaseErrorCodes.COMMENT_NOT_FOUND);
        }
        assertOwnerOrModerator(comment.getAuthorId());
        comment.setStatus(STATUS_DELETED);
        commentMapper.updateById(comment);
    }

    private ForumPost requirePost(Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_FOUND);
        }
        return post;
    }

    private void requireActivePost(Long postId) {
        if (requirePost(postId).getStatus() != STATUS_NORMAL) {
            throw new BusinessException(BaseErrorCodes.POST_NOT_ACTIVE);
        }
    }

    private boolean canModerate() {
        return CurrentUserContext.require().hasPermission(MODERATE_PERMISSION);
    }


    /** 仅作者本人或拥有管理权限的用户可操作 */
    private void assertOwnerOrModerator(Long authorId) {
        AuthSession session = CurrentUserContext.require();
        if (!Objects.equals(session.userId(), authorId) && !session.hasPermission(MODERATE_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
    }
}
