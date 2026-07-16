package com.smartcampus.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.config.AuthProperties;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.model.IssuedToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthProperties properties;
    private ObjectMapper objectMapper;
    private SessionTokenService tokenService;
    private AuthSession session;

    @BeforeEach
    void setUp() {
        properties = new AuthProperties();
        properties.setSessionTtl(Duration.ofMinutes(30));
        objectMapper = new ObjectMapper();
        tokenService = new SessionTokenService(redisTemplate, objectMapper, properties);
        session = new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of("student:read"), 1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldStoreOnlyTokenHashInRedisKey() {
        IssuedToken issuedToken = tokenService.create(session);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(valueOperations).set(keyCaptor.capture(), anyString(), org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(30)));

        assertThat(issuedToken.token()).hasSize(43);
        assertThat(keyCaptor.getValue()).startsWith("auth:session:");
        assertThat(keyCaptor.getValue()).doesNotContain(issuedToken.token());
        assertThat(issuedToken.expiresIn()).isEqualTo(1800);
    }

    @Test
    void shouldReadAndRefreshSessionTtl() throws Exception {
        when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(session));

        Optional<AuthSession> result = tokenService.find("raw-token");

        assertThat(result).contains(session);
        verify(redisTemplate).expire(anyString(), org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(30)));
    }
}
