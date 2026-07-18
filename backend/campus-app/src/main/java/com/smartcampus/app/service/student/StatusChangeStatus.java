package com.smartcampus.app.service.student;

import java.util.Arrays;

public enum StatusChangeStatus {
    DRAFT(0),
    COUNSELOR_REVIEW(1),
    ACADEMIC_REVIEW(2),
    APPROVED(3),
    COUNSELOR_REJECTED(4),
    ACADEMIC_REJECTED(5),
    WITHDRAWN(6);

    private final int code;

    StatusChangeStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static StatusChangeStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status-change status: " + code));
    }

    public static StatusChangeStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return valueOf(name.toUpperCase());
    }
}
