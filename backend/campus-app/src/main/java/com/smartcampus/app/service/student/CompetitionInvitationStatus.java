package com.smartcampus.app.service.student;

public enum CompetitionInvitationStatus {
    INVITED(0),
    ACCEPTED(1),
    DECLINED(2),
    REMOVED(3);

    private final int code;

    CompetitionInvitationStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static CompetitionInvitationStatus fromCode(Integer code) {
        for (CompetitionInvitationStatus status : values()) {
            if (code != null && status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown competition invitation status: " + code);
    }
}
