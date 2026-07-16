package com.smartcampus.auth.service;

import com.smartcampus.auth.model.IssuedToken;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.LoginRequest;
import com.smartcampus.contract.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthUserMapper userMapper;

    @Mock
    private SessionTokenService tokenService;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationService(userMapper, passwordEncoder, tokenService);
    }

    @Test
    void shouldLoginWithBcryptPasswordAndReturnSession() {
        UserEntity user = user("600001", passwordEncoder.encode("123321"));
        when(userMapper.findActiveByUsername("600001")).thenReturn(user);
        when(userMapper.findRoleCodes(1L)).thenReturn(List.of("STUDENT"));
        when(userMapper.findPermissionCodes(1L)).thenReturn(List.of("student:read"));
        when(userMapper.findMenus(1L)).thenReturn(List.of());
        when(tokenService.create(any())).thenReturn(new IssuedToken("token-value", 7200));

        LoginRequest request = loginRequest("600001", "123321");
        var result = authenticationService.login(request);

        assertThat(result.getToken()).isEqualTo("token-value");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getCurrentUser().getRoles()).containsExactly("STUDENT");
    }

    @Test
    void shouldRejectInvalidPasswordWithoutExposingAccountState() {
        when(userMapper.findActiveByUsername("600001")).thenReturn(user("600001", passwordEncoder.encode("123321")));

        assertThatThrownBy(() -> authenticationService.login(loginRequest("600001", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号或密码错误");
    }

    private UserEntity user(String username, String password) {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUsername(username);
        user.setPassword(password);
        user.setUserType(1);
        user.setStatus(1);
        return user;
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
