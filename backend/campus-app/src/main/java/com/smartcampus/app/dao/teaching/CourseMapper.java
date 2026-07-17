package com.smartcampus.app.dao.teaching;

import com.smartcampus.app.dto.teaching.CourseSearchQueryDto;
import com.smartcampus.app.vo.teaching.CourseVo;
import com.smartcampus.contract.entity.CourseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CourseMapper extends BaseMapper<CourseEntity> {

    /**
     * 查询课程列表（含排课时间、教室、容量、教师名）
     */
    List<Map<String, Object>> selectCourseListWithDetails(@Param("semester") String semester);

    /**
     * 多维搜索课程 —— 根据 searchType 动态拼接条件，支持分页。
     * <p>使用 MyBatis XML 中的动态 SQL 实现。</p>
     */
    IPage<CourseVo> searchCourses(Page<CourseVo> page, @Param("query") CourseSearchQueryDto query);
}
