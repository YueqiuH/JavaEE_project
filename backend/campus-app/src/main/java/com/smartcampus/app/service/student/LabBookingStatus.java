package com.smartcampus.app.service.student;

public enum LabBookingStatus {
    RESERVED(1),
    CANCELLED(2),
    CHECKED_OUT(3),
    CHECKED_IN(4),
    EXPIRED(5);

    private final int code;

    LabBookingStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static LabBookingStatus fromCode(Integer code) {
        if (code != null) {
            for (LabBookingStatus status : values()) {
                if (status.code == code) return status;
            }
        }
        throw new IllegalArgumentException("Unknown lab booking status: " + code);
    }
}
