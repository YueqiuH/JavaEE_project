package com.smartcampus.app.service.student;

import java.util.Arrays;

public enum ScholarshipStatus {
    DRAFT(0),
    SUBMITTED(1),
    RETURNED(2),
    APPROVED(3),
    REJECTED(4),
    WITHDRAWN(5),
    SELECTED(6),
    ACADEMIC_REVIEW(7);

    private final int code;

    ScholarshipStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ScholarshipStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown scholarship status: " + code));
    }

    public static ScholarshipStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return valueOf(name.toUpperCase());
    }
}
