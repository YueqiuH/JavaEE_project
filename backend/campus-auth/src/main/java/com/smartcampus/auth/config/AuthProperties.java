package com.smartcampus.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private Duration sessionTtl = Duration.ofHours(2);
    private boolean slidingExpiration = true;
    private String redisKeyPrefix = "auth:session:";
    private List<String> publicPaths = new ArrayList<>(List.of("/api/v1/auth/login"));

    public Duration getSessionTtl() {
        return sessionTtl;
    }

    public void setSessionTtl(Duration sessionTtl) {
        this.sessionTtl = sessionTtl;
    }

    public boolean isSlidingExpiration() {
        return slidingExpiration;
    }

    public void setSlidingExpiration(boolean slidingExpiration) {
        this.slidingExpiration = slidingExpiration;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
