package com.smartcampus.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.auth.config.AuthProperties;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.model.IssuedToken;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class SessionTokenService {

    private static final Logger log = LoggerFactory.getLogger(SessionTokenService.class);
    private static final int TOKEN_BYTES = 32;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionTokenService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            AuthProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public IssuedToken create(AuthSession session) {
        String rawToken = newToken();
        try {
            String serializedSession = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(redisKey(rawToken), serializedSession, properties.getSessionTtl());
            return new IssuedToken(rawToken, properties.getSessionTtl().toSeconds());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<AuthSession> find(String rawToken) {
        String key = redisKey(rawToken);
        String serializedSession = redisTemplate.opsForValue().get(key);
        if (serializedSession == null) {
            return Optional.empty();
        }

        try {
            AuthSession session = objectMapper.readValue(serializedSession, AuthSession.class);
            if (properties.isSlidingExpiration()) {
                redisTemplate.expire(key, properties.getSessionTtl());
            }
            return Optional.of(session);
        } catch (JsonProcessingException exception) {
            log.warn("删除无法反序列化的认证会话，redisKey={}", key);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void revoke(String rawToken) {
        redisTemplate.delete(redisKey(rawToken));
    }

    private String newToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String redisKey(String rawToken) {
        return properties.getRedisKeyPrefix() + sha256(rawToken);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }
}
