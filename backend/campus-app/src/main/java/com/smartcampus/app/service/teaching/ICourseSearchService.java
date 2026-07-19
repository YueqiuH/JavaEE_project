package com.smartcampus.app.service.teaching;

import com.smartcampus.app.dto.teaching.CourseSearchQueryDto;
import com.smartcampus.common.result.CommonResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 课程多维度搜索服务。
 */
public interface ICourseSearchService {

    /**
     * 按指定维度搜索课程并分页返回。
     */
    CommonResult<IPage<?>> search(CourseSearchQueryDto query);
}
