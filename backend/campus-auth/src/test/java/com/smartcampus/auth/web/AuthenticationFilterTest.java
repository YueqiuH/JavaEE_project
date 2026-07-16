package com.smartcampus.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.config.AuthProperties;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.service.SessionTokenService;
import com.smartcampus.auth.token.BearerTokenResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationFilterTest {

    private final SessionTokenService tokenService = mock(SessionTokenService.class);
    private final AuthenticationFilter filter = new AuthenticationFilter(
            new BearerTokenResolver(),
            tokenService,
            new AuthProperties(),
            new ObjectMapper()
    );

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void shouldReturnStandardUnauthorizedResponseWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student/scholarships");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
        });

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":401001");
        assertThat(response.getContentAsString()).contains("\"requestId\"");
    }

    @Test
    void shouldExposeSessionOnlyDuringAuthenticatedRequest() throws Exception {
        AuthSession session = new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of("student:read"), 1L);
        when(tokenService.find("valid-token")).thenReturn(Optional.of(session));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student/scholarships");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            assertThat(CurrentUserContext.require().username()).isEqualTo("600001");
        });

        assertThat(CurrentUserContext.current()).isEmpty();
    }
}
