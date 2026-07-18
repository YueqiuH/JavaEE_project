package com.smartcampus.app.service.student;

public enum CompetitionTeamStatus {
    FORMING(0),
    SUBMITTED(1),
    APPROVED(2),
    RETURNED(3),
    REJECTED(4);

    private final int code;

    CompetitionTeamStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static CompetitionTeamStatus fromCode(Integer code) {
        for (CompetitionTeamStatus status : values()) {
            if (code != null && status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown competition team status: " + code);
    }

    public static CompetitionTeamStatus fromName(String name) {
        return name == null || name.isBlank() ? null : valueOf(name.toUpperCase());
    }
}
