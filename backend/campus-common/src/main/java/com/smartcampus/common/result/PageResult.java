package com.smartcampus.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> records;
    private final long total;
    private final long page;
    private final long size;

    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
