package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.ForumService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.ForumCommentCreateRequest;
import com.smartcampus.contract.dto.ForumPostCreateRequest;
import com.smartcampus.contract.entity.ForumPost;
import com.smartcampus.contract.vo.ForumCommentVo;
import com.smartcampus.contract.vo.ForumPostVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/base/forum")
@Tag(name = "基础数据 - 校园论坛")
@SecurityRequirement(name = "bearerAuth")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/posts")
    @Operation(summary = "分页查询帖子", description = "普通用户仅可见正常帖子；管理员可按状态筛选（1=正常, 0=已删除, -1=已封禁）；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<ForumPostVo>> pagePosts(
            @Valid @ParameterObject PageParam pageParam,
            @Parameter(description = "标题/内容关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态过滤（需管理权限）") @RequestParam(required = false) Integer status) {
        return CommonResult.successPageData(forumService.pagePosts(pageParam, keyword, status));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "查看帖子详情", description = "浏览数 +1；错误示例：404106 帖子不存在或已删除")
    @RequirePermission("base:read")
    public CommonResult<ForumPostVo> getPost(@PathVariable Long postId) {
        return CommonResult.success(forumService.getPost(postId));
    }

    @PostMapping("/posts")
    @Operation(summary = "发布帖子", description = "作者取当前登录用户")
    @RequirePermission("base:read")
    public CommonResult<ForumPost> createPost(@Valid @RequestBody ForumPostCreateRequest request) {
        return CommonResult.success(forumService.createPost(request));
    }

    @PostMapping("/posts/{postId}/likes")
    @Operation(summary = "点赞帖子", description = "错误示例：409108 帖子已封禁或删除，无法操作")
    @RequirePermission("base:read")
    public CommonResult<Void> likePost(@PathVariable Long postId) {
        forumService.likePost(postId);
        return CommonResult.success();
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "删除帖子", description = "作者本人或管理员可删（软删除）；错误示例：403001 没有该操作权限")
    @RequirePermission("base:read")
    public CommonResult<Void> deletePost(@PathVariable Long postId) {
        forumService.deletePost(postId);
        return CommonResult.success();
    }

    @PutMapping("/posts/{postId}/status")
    @Operation(summary = "封禁/恢复帖子", description = "管理员操作，body 形如 {\"status\": -1}（1=正常, 0=删除, -1=封禁）；错误示例：400001 请求参数不正确")
    @RequirePermission("base:write")
    public CommonResult<Void> moderatePost(@PathVariable Long postId,
                                           @RequestBody Map<String, Integer> body) {
        forumService.moderatePost(postId, body.get("status"));
        return CommonResult.success();
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "查询帖子回复列表", description = "错误示例：404106 帖子不存在或已删除")
    @RequirePermission("base:read")
    public CommonResult<List<ForumCommentVo>> listComments(@PathVariable Long postId) {
        return CommonResult.success(forumService.listComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "回复帖子", description = "回复人取当前登录用户；错误示例：409108 帖子已封禁或删除，无法操作")
    @RequirePermission("base:read")
    public CommonResult<ForumCommentVo> addComment(@PathVariable Long postId,
                                                   @Valid @RequestBody ForumCommentCreateRequest request) {
        return CommonResult.success(forumService.addComment(postId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除回复", description = "作者本人或管理员可删（软删除）；错误示例：404107 回复不存在或已删除")
    @RequirePermission("base:read")
    public CommonResult<Void> deleteComment(@PathVariable Long commentId) {
        forumService.deleteComment(commentId);
        return CommonResult.success();
    }
}
