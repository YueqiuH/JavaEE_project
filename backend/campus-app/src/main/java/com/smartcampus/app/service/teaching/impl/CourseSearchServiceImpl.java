package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.app.dto.teaching.CourseSearchQueryDto;
import com.smartcampus.app.service.teaching.ICourseSearchService;
import com.smartcampus.app.vo.teaching.CourseVo;
import com.smartcampus.common.result.CommonResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课程搜索 —— 高容错、模糊匹配 + 精确搜索 + 分页。
 */
@Service
public class CourseSearchServiceImpl implements ICourseSearchService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public CommonResult<IPage<?>> search(CourseSearchQueryDto query) {

        // ---- 参数默认值处理 ----
        int pageNum = (query.getPageNum() == null || query.getPageNum() < 1) ? 1 : query.getPageNum();
        int pageSize = (query.getPageSize() == null || query.getPageSize() < 1) ? 10 : query.getPageSize();

        // 非法 searchType 修正为空(走默认逻辑)
        if (query.getSearchType() != null
            && !"course_name".equals(query.getSearchType())
            && !"course_code".equals(query.getSearchType())
            && !"teacher_name".equals(query.getSearchType())) {
            query.setSearchType(null);
        }

        // ---- 执行分页搜索 ----
        Page<CourseVo> page = new Page<>(pageNum, pageSize);
        IPage<CourseVo> result = courseMapper.searchCourses(page, query);

        return CommonResult.success(result);
    }
}
