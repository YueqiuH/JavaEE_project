package com.smartcampus.app.service.student;

public enum LabBookingStatus {
    BOOKED(1),
    CANCELLED(2),
    COMPLETED(3);

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
