package com.smartcampus.auth.context;

import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;

import java.util.Optional;

public final class CurrentUserContext {

    private static final ThreadLocal<AuthSession> CURRENT_SESSION = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(AuthSession session) {
        CURRENT_SESSION.set(session);
    }

    public static Optional<AuthSession> current() {
        return Optional.ofNullable(CURRENT_SESSION.get());
    }

    public static AuthSession require() {
        return current().orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.UNAUTHORIZED));
    }

    public static void clear() {
        CURRENT_SESSION.remove();
    }
}
