package com.smartcampus.contract.dto;

import com.smartcampus.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生信息库多条件分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentQuery extends PageParam {

    /** 姓名/学号关键字 */
    private String keyword;

    private Long deptId;

    private Long majorId;

    /** 入学年份 */
    private Integer enrollYear;

    /** 学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学 */
    private Integer status;
}
