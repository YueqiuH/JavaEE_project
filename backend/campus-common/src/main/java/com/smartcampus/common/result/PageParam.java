package com.smartcampus.common.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageParam implements Serializable {

    private static final Integer PAGE_NO = 1;
    private static final Integer PAGE_SIZE = 10;

    /**
     * 每页条数 - 不分页
     */
    public static final Integer PAGE_SIZE_NONE = -1;

    private Integer pageNo = PAGE_NO;

    private Integer pageSize = PAGE_SIZE;

}
