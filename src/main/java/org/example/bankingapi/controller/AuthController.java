package org.example.bankingapi.controller;

import org.example.bankingapi.dto.request.LoginRequest;
import org.example.bankingapi.dto.request.RefreshTokenRequest;
import org.example.bankingapi.dto.request.RegisterRequest;
import org.example.bankingapi.dto.request.ForgotPasswordRequest;
import org.example.bankingapi.dto.request.VerifyOtpRequest;
import org.example.bankingapi.dto.request.ResetPasswordRequest;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.AuthResponse;
import org.example.bankingapi.service.AuthService;
import org.example.bankingapi.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ApiDataResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng nhập thành công",
                authService.login(request),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiDataResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng ký tài khoản thành công",
                authService.register(request),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiDataResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cấp lại access token thành công",
                authService.refreshToken(request),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiDataResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng xuất thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // Bước 1: Nhập email → gửi OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiDataResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendOtp(request);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Mã OTP đã được gửi đến email của bạn. Có hiệu lực trong 5 phút.",
                null, null, HttpStatus.OK
        ), HttpStatus.OK);
    }

    // Bước 2: Xác minh OTP → nhận reset token
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiDataResponse<String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        String resetToken = passwordResetService.verifyOtp(request);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "OTP hợp lệ. Sử dụng reset token để đặt lại mật khẩu.",
                resetToken, null, HttpStatus.OK
        ), HttpStatus.OK);
    }

    // Bước 3: Đặt lại mật khẩu bằng reset token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiDataResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.",
                null, null, HttpStatus.OK
        ), HttpStatus.OK);
    }
}