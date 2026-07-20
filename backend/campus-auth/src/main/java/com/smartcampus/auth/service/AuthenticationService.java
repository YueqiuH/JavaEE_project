package com.smartcampus.auth.service;

import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.model.IssuedToken;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.LoginRequest;
import com.smartcampus.contract.dto.UpdatePasswordRequest;
import com.smartcampus.contract.dto.UpdateProfileRequest;
import com.smartcampus.contract.entity.Menu;
import com.smartcampus.contract.entity.User;
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
        User user = userMapper.findActiveByUsername(request.getUsername());
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
        User user = userMapper.findActiveById(session.userId());
        if (user == null) {
            throw new BusinessException(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        return loadCurrentUser(user);
    }

    public void changePassword(UpdatePasswordRequest request) {
        AuthSession session = CurrentUserContext.require();
        User user = userMapper.findActiveById(session.userId());
        if (user == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(GlobalErrorCodeConstants.LOGIN_ERROR);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    public CurrentUserVo updateProfile(UpdateProfileRequest request) {
        AuthSession session = CurrentUserContext.require();
        User user = userMapper.findActiveById(session.userId());
        if (user == null) {
            throw new BusinessException(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        userMapper.updateById(user);
        return loadCurrentUser(user);
    }

    private CurrentUserVo loadCurrentUser(User user) {
        Set<String> roles = new LinkedHashSet<>(userMapper.findRoleCodes(user.getUserId()));
        Set<String> permissions = new LinkedHashSet<>(userMapper.findPermissionCodes(user.getUserId()));
        List<Menu> menus = userMapper.findMenus(user.getUserId());
        return new CurrentUserVo(user, roles, permissions, menus);
    }
}
