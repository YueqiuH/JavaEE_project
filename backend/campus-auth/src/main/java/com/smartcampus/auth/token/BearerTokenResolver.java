package com.smartcampus.auth.token;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BearerTokenResolver {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public Optional<String> resolve(HttpServletRequest request) {
        return resolve(request.getHeader(AUTHORIZATION_HEADER));
    }

    public Optional<String> resolve(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty() || token.contains(" ")) {
            return Optional.empty();
        }
        return Optional.of(token);
    }
}
