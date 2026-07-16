package com.smartcampus.auth.model;

import java.util.Set;

public record AuthSession(
        Long userId,
        String username,
        Integer userType,
        Set<String> roles,
        Set<String> permissions,
        long issuedAtEpochSecond
) {

    public AuthSession {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
