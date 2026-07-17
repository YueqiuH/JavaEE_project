package com.smartcampus.common.web;

import org.slf4j.MDC;

import java.util.UUID;

public final class RequestIdContext {

    public static final String MDC_KEY = "requestId";

    private RequestIdContext() {
    }

    public static String currentOrCreate() {
        String requestId = MDC.get(MDC_KEY);
        return requestId == null || requestId.isBlank() ? newRequestId() : requestId;
    }

    public static String newRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
