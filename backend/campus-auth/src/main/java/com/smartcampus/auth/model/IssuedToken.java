package com.smartcampus.auth.model;

public record IssuedToken(String token, long expiresIn) {
}
