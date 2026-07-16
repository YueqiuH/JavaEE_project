package com.smartcampus.common.result;

import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.web.RequestIdContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     *
     * @see ErrorCode#getCode()
     */
    private final Integer code;

    /**
     * 面向调用方的提示信息。
     */
    private final String message;

    /**
     * 返回数据。
     */
    private final T data;

    /**
     * 请求链路标识。
     */
    private final String requestId;

    private CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = RequestIdContext.currentOrCreate();
    }

    /**
     * 将传入的 result 对象，转换成另外一个泛型结果的对象
     *
     * 因为 A 方法返回的 CommonResult 对象，不满足调用其的 B 方法的返回，所以需要进行转换。
     *
     * @param result 传入的 result 对象
     * @param <T>    返回的泛型
     * @return 新的 CommonResult 对象
     */
    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMessage());
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        if (GlobalErrorCodeConstants.SUCCESS.getCode().equals(code)) {
            throw new IllegalArgumentException("错误响应不能使用成功代码");
        }
        return new CommonResult<>(code, message, null);
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> CommonResult<PageResult<T>> successPageData(IPage<T> page) {
        return success(PageResult.from(page));
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(
                GlobalErrorCodeConstants.SUCCESS.getCode(),
                GlobalErrorCodeConstants.SUCCESS.getMessage(),
                data
        );
    }

    public static <T> CommonResult<T> success() {
        return success(null);
    }

    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.getCode());
    }

    @JsonIgnore // 避免 jackson 序列化
    public boolean isSuccess() {
        return isSuccess(code);
    }

    @JsonIgnore // 避免 jackson 序列化
    public boolean isError() {
        return !isSuccess();
    }
}
