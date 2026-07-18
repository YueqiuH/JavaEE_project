package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.NewsService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.NewsSaveRequest;
import com.smartcampus.contract.entity.News;
import com.smartcampus.contract.vo.NewsVo;
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

@RestController
@RequestMapping("/api/v1/base/news")
@Tag(name = "基础数据 - 新闻公告")
@SecurityRequirement(name = "bearerAuth")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    @Operation(summary = "分页查询新闻公告", description = "置顶优先、按发布时间倒序；错误示例：401001 账号未登录")
    @RequirePermission("forum:read")
    public CommonResult<PageResult<NewsVo>> page(
            @Valid @ParameterObject PageParam pageParam,
            @Parameter(description = "类型：公告/新闻") @RequestParam(required = false) String newsType,
            @Parameter(description = "标题/内容关键字") @RequestParam(required = false) String keyword) {
        return CommonResult.successPageData(newsService.pageVo(pageParam, newsType, keyword));
    }

    @PostMapping
    @Operation(summary = "发布新闻公告", description = "发布人取当前登录用户；错误示例：403001 没有该操作权限")
    @RequirePermission("base:write")
    public CommonResult<News> create(@Valid @RequestBody NewsSaveRequest request) {
        return CommonResult.success(newsService.create(request));
    }

    @PutMapping("/{newsId}")
    @Operation(summary = "修改新闻公告", description = "错误示例：404105 新闻公告不存在")
    @RequirePermission("base:write")
    public CommonResult<News> update(@PathVariable Long newsId,
                                     @Valid @RequestBody NewsSaveRequest request) {
        return CommonResult.success(newsService.update(newsId, request));
    }

    @DeleteMapping("/{newsId}")
    @Operation(summary = "删除新闻公告", description = "错误示例：404105 新闻公告不存在")
    @RequirePermission("base:write")
    public CommonResult<Void> delete(@PathVariable Long newsId) {
        newsService.delete(newsId);
        return CommonResult.success();
    }
}
