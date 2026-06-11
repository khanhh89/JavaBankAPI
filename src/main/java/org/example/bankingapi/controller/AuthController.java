package org.example.bankingapi.controller;

import org.example.bankingapi.dto.request.LoginRequest;
import org.example.bankingapi.dto.request.RefreshTokenRequest;
import org.example.bankingapi.dto.request.RegisterRequest;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.AuthResponse;
import org.example.bankingapi.service.AuthService;
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
        String token = authHeader.substring(7); // Remove "Bearer "
        authService.logout(token);

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng xuất thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}