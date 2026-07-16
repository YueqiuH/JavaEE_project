package com.smartcampus.common.result;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

@Data
public class PageParam implements Serializable {

    private static final Integer DEFAULT_PAGE = 1;
    private static final Integer DEFAULT_SIZE = 20;

    @Min(value = 1, message = "page 必须大于等于 1")
    private Integer page = DEFAULT_PAGE;

    @Min(value = 1, message = "size 必须大于等于 1")
    @Max(value = 200, message = "size 不能大于 200")
    private Integer size = DEFAULT_SIZE;

}
