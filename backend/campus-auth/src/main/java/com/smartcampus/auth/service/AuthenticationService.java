package com.smartcampus.auth.service;

import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.model.IssuedToken;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.LoginRequest;
import com.smartcampus.contract.entity.MenuEntity;
import com.smartcampus.contract.entity.UserEntity;
import com.smartcampus.contract.vo.CurrentUserVo;
import com.smartcampus.contract.vo.LoginUserVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthenticationService {

    private final AuthUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService tokenService;

    public AuthenticationService(
            AuthUserMapper userMapper,
            PasswordEncoder passwordEncoder,
            SessionTokenService tokenService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginUserVo login(LoginRequest request) {
        UserEntity user = userMapper.findActiveByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(GlobalErrorCodeConstants.LOGIN_ERROR);
        }

        CurrentUserVo currentUser = loadCurrentUser(user);
        AuthSession session = new AuthSession(
                user.getUserId(),
                user.getUsername(),
                user.getUserType(),
                currentUser.getRoles(),
                currentUser.getPermissions(),
                Instant.now().getEpochSecond()
        );
        IssuedToken issuedToken = tokenService.create(session);
        return new LoginUserVo(issuedToken.token(), "Bearer", issuedToken.expiresIn(), currentUser);
    }

    public void logout(String rawToken) {
        tokenService.revoke(rawToken);
    }

    public CurrentUserVo currentUser() {
        AuthSession session = CurrentUserContext.require();
        UserEntity user = userMapper.findActiveById(session.userId());
        if (user == null) {
            throw new BusinessException(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        return loadCurrentUser(user);
    }

    private CurrentUserVo loadCurrentUser(UserEntity user) {
        Set<String> roles = new LinkedHashSet<>(userMapper.findRoleCodes(user.getUserId()));
        Set<String> permissions = new LinkedHashSet<>(userMapper.findPermissionCodes(user.getUserId()));
        List<MenuEntity> menus = userMapper.findMenus(user.getUserId());
        return new CurrentUserVo(user, roles, permissions, menus);
    }
}
