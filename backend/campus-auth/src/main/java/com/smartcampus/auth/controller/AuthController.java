package com.smartcampus.auth.controller;

import com.smartcampus.auth.service.AuthenticationService;
import com.smartcampus.auth.token.BearerTokenResolver;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.dto.LoginRequest;
import com.smartcampus.contract.dto.ResetPasswordRequest;
import com.smartcampus.contract.dto.UpdatePasswordRequest;
import com.smartcampus.contract.dto.UpdateProfileRequest;
import com.smartcampus.contract.vo.CurrentUserVo;
import com.smartcampus.contract.vo.LoginUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final BearerTokenResolver tokenResolver;

    public AuthController(AuthenticationService authenticationService, BearerTokenResolver tokenResolver) {
        this.authenticationService = authenticationService;
        this.tokenResolver = tokenResolver;
    }

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public CommonResult<LoginUserVo> login(@Valid @RequestBody LoginRequest request) {
        return CommonResult.success(authenticationService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult<Void> logout(HttpServletRequest request) {
        String rawToken = tokenResolver.resolve(request)
                .orElseThrow(() -> new BusinessException(GlobalErrorCodeConstants.UNAUTHORIZED));
        authenticationService.logout(rawToken);
        return CommonResult.success();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult<CurrentUserVo> currentUser() {
        return CommonResult.success(authenticationService.currentUser());
    }

    @PostMapping("/reset-password/send-code")
    @Operation(summary = "忘记密码 - 发送验证码（演示模式返回明文）")
    public CommonResult<Map<String, Object>> sendResetCode(@RequestBody Map<String, String> body) {
        int code = authenticationService.sendResetCode(
                body.get("username"), body.get("phone"));
        return CommonResult.success(Map.of("code", String.valueOf(code), "message", "演示模式：验证码为 " + code));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "忘记密码 - 验证码+新密码重置")
    public CommonResult<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return CommonResult.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult<Void> changePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authenticationService.changePassword(request);
        return CommonResult.success();
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult<CurrentUserVo> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return CommonResult.success(authenticationService.updateProfile(request));
    }
}
