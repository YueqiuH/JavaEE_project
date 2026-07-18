package com.smartcampus.app.service.student;

public enum CompetitionStatus {
    DRAFT(0),
    OPEN(1),
    CLOSED(2);

    private final int code;

    CompetitionStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static CompetitionStatus fromCode(Integer code) {
        for (CompetitionStatus status : values()) {
            if (code != null && status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown competition status: " + code);
    }

    public static CompetitionStatus fromName(String name) {
        return name == null || name.isBlank() ? null : valueOf(name.toUpperCase());
    }
}
