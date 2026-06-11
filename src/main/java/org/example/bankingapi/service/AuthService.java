package org.example.bankingapi.service;

import org.example.bankingapi.dto.request.LoginRequest;
import org.example.bankingapi.dto.request.RefreshTokenRequest;
import org.example.bankingapi.dto.request.RegisterRequest;
import org.example.bankingapi.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
}
