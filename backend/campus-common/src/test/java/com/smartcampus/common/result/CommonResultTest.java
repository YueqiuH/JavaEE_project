package com.smartcampus.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.web.RequestIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonResultTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldCreateStandardSuccessResponse() {
        MDC.put(RequestIdContext.MDC_KEY, "request-1234");

        CommonResult<String> result = CommonResult.success("ok");

        assertThat(result.getCode()).isZero();
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("ok");
        assertThat(result.getRequestId()).isEqualTo("request-1234");
    }

    @Test
    void shouldWrapPageMetadataInsideData() {
        Page<String> page = new Page<>(2, 20, 41);
        page.setRecords(List.of("record"));

        CommonResult<PageResult<String>> result = CommonResult.successPageData(page);

        assertThat(result.getData().getRecords()).containsExactly("record");
        assertThat(result.getData().getTotal()).isEqualTo(41);
        assertThat(result.getData().getPage()).isEqualTo(2);
        assertThat(result.getData().getSize()).isEqualTo(20);
    }
}
