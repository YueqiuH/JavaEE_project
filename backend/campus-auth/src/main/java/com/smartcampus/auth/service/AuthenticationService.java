package com.smartcampus.auth.service;

import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.model.IssuedToken;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.LoginRequest;
import com.smartcampus.contract.dto.ResetPasswordRequest;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, CodeEntry> resetCodes = new ConcurrentHashMap<>();

    private record CodeEntry(int code, long expiresAt) {}

    /** Generate a 6-digit demo code, valid for 1 minute. */
    public int sendResetCode(String username, String phone) {
        User user = userMapper.findActiveByUsername(username);
        boolean phoneOk = (user != null && phone.equals(user.getPhone()))
                || userMapper.findStudentPhone(username, phone) > 0;
        if (!phoneOk) throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400003, "账号或手机号不匹配", org.springframework.http.HttpStatus.BAD_REQUEST));
        int code = 100000 + (int) (Math.random() * 900000);
        resetCodes.put(username, new CodeEntry(code, System.currentTimeMillis() + 60_000));
        return code;
    }

    public void resetPassword(ResetPasswordRequest request) {
        CodeEntry entry = resetCodes.get(request.getUsername());
        if (entry == null) {
            throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400004, "请先获取验证码", org.springframework.http.HttpStatus.BAD_REQUEST));
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            resetCodes.remove(request.getUsername());
            throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400004, "验证码已失效，请重新获取", org.springframework.http.HttpStatus.BAD_REQUEST));
        }
        if (entry.code != Integer.parseInt(request.getCode())) {
            throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400004, "验证码错误", org.springframework.http.HttpStatus.BAD_REQUEST));
        }
        User user = userMapper.findActiveByUsername(request.getUsername());
        if (user == null) throw new BusinessException(GlobalErrorCodeConstants.LOGIN_ERROR);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        resetCodes.remove(request.getUsername());
    }

    public void changePassword(UpdatePasswordRequest request) {
        AuthSession session = CurrentUserContext.require();
        User user = userMapper.findActiveById(session.userId());
        if (user == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400005, "原密码错误", org.springframework.http.HttpStatus.BAD_REQUEST));
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(new com.smartcampus.common.result.ErrorCode(400006, "新密码不能与原密码相同", org.springframework.http.HttpStatus.BAD_REQUEST));
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
