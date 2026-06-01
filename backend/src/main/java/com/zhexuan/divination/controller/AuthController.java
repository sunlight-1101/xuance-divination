package com.zhexuan.divination.controller;

import com.zhexuan.divination.common.PageResult;
import com.zhexuan.divination.common.Result;
import com.zhexuan.divination.dto.ChangePasswordDTO;
import com.zhexuan.divination.dto.EmailCodeDTO;
import com.zhexuan.divination.dto.LoginDTO;
import com.zhexuan.divination.dto.RegisterDTO;
import com.zhexuan.divination.dto.ResetPasswordDTO;
import com.zhexuan.divination.dto.UpdateNicknameDTO;
import com.zhexuan.divination.dto.UserProfileDTO;
import com.zhexuan.divination.service.AuthService;
import com.zhexuan.divination.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/send-email-code")
    public Result<Boolean> sendEmailCode(@RequestBody EmailCodeDTO dto) {
        return Result.ok(authService.sendRegisterEmailCode(dto));
    }

    @PostMapping("/send-reset-code")
    public Result<Boolean> sendResetCode(@RequestBody EmailCodeDTO dto) {
        return Result.ok(authService.sendResetPasswordEmailCode(dto));
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Boolean> logout() {
        return Result.ok(true);
    }

    @PostMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody UserProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        return Result.ok(authService.updateProfile(dto));
    }

    @PostMapping("/nickname")
    public Result<UserVO> updateNickname(@RequestBody UpdateNicknameDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        return Result.ok(authService.updateNickname(dto));
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        return Result.ok(authService.changePassword(dto));
    }

    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@RequestBody ResetPasswordDTO dto) {
        return Result.ok(authService.resetPassword(dto));
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> listUsers(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(authService.listAllUsers(userId, keyword, page, size));
    }
}
