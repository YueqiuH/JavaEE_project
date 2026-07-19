package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.NewsSaveRequest;
import com.smartcampus.contract.entity.News;
import com.smartcampus.contract.vo.NewsVo;

public interface NewsService {

    IPage<NewsVo> pageVo(PageParam pageParam, String newsType, String keyword);

    News create(NewsSaveRequest request);

    News update(Long newsId, NewsSaveRequest request);

    void delete(Long newsId);
}
