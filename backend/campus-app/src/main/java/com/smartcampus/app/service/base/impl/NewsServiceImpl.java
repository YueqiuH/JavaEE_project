package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.NewsMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.NewsService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.NewsSaveRequest;
import com.smartcampus.contract.entity.News;
import com.smartcampus.contract.vo.NewsVo;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;

    public NewsServiceImpl(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    @Override
    public IPage<NewsVo> pageVo(PageParam pageParam, String newsType, String keyword) {
        Page<NewsVo> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        return newsMapper.selectVoPage(page, newsType, keyword);
    }

    @Override
    public News create(NewsSaveRequest request) {
        News news = new News();
        applyRequest(news, request);
        news.setPublisherId(CurrentUserContext.require().userId());
        newsMapper.insert(news);
        return news;
    }

    @Override
    public News update(Long newsId, NewsSaveRequest request) {
        News news = requireNews(newsId);
        applyRequest(news, request);
        newsMapper.updateById(news);
        return news;
    }

    @Override
    public void delete(Long newsId) {
        requireNews(newsId);
        newsMapper.deleteById(newsId);
    }

    private News requireNews(Long newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(BaseErrorCodes.NEWS_NOT_FOUND);
        }
        return news;
    }

    private void applyRequest(News news, NewsSaveRequest request) {
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setNewsType(request.getNewsType());
        news.setIsPinned(request.getIsPinned() == null ? 0 : request.getIsPinned());
    }
}
