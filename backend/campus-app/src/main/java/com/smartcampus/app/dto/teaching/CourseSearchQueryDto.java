package com.smartcampus.app.dto.teaching;

import lombok.Data;

/**
 * 课程搜索查询参数。
 */
@Data
public class CourseSearchQueryDto {

    /** 搜索关键字（模糊匹配时生效） */
    private String keyword;

    /**
     * 搜索类型：
     * <ul>
     *   <li>{@code course_name}  — 课程名称模糊搜索</li>
     *   <li>{@code course_code}  — 课程代码精确搜索</li>
     *   <li>{@code teacher_name} — 授课教师姓名模糊搜索（关联 user 表）</li>
     * </ul>
     */
    private String searchType;

    /** 课程分类过滤：必修 / 选修 / 限选。为空则不过滤 */
    private String classification;

    /** 页码，从 1 开始 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;
}
