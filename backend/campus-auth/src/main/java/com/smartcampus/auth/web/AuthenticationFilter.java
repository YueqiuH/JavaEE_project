package com.smartcampus.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.config.AuthProperties;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.service.SessionTokenService;
import com.smartcampus.auth.token.BearerTokenResolver;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String API_PREFIX = "/api/v1/";

    private final BearerTokenResolver tokenResolver;
    private final SessionTokenService tokenService;
    private final AuthProperties properties;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(
            BearerTokenResolver tokenResolver,
            SessionTokenService tokenService,
            AuthProperties properties,
            ObjectMapper objectMapper
    ) {
        this.tokenResolver = tokenResolver;
        this.tokenService = tokenService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith(API_PREFIX)
                || properties.getPublicPaths().contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> rawToken = tokenResolver.resolve(request);
        if (rawToken.isEmpty()) {
            writeError(response, GlobalErrorCodeConstants.UNAUTHORIZED);
            return;
        }

        Optional<AuthSession> session;
        try {
            session = tokenService.find(rawToken.get());
        } catch (RuntimeException exception) {
            log.error("认证会话存储不可用", exception);
            writeError(response, GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
            return;
        }
        if (session.isEmpty()) {
            writeError(response, GlobalErrorCodeConstants.UNAUTHORIZED);
            return;
        }

        CurrentUserContext.set(session.get());
        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), CommonResult.error(errorCode));
    }
}
