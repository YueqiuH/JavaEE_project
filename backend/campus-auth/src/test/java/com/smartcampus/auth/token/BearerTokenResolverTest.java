package com.smartcampus.auth.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BearerTokenResolverTest {

    private final BearerTokenResolver resolver = new BearerTokenResolver();

    @Test
    void shouldResolveBearerToken() {
        assertThat(resolver.resolve("Bearer header.payload.signature"))
                .contains("header.payload.signature");
    }

    @Test
    void shouldRejectLegacyOrMalformedHeaders() {
        assertThat(resolver.resolve("Token legacy-token")).isEmpty();
        assertThat(resolver.resolve("Bearer ")).isEmpty();
        assertThat(resolver.resolve("Bearer token with spaces")).isEmpty();
    }
}
