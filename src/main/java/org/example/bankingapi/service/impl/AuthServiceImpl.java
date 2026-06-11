package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.request.LoginRequest;
import org.example.bankingapi.dto.request.RefreshTokenRequest;
import org.example.bankingapi.dto.request.RegisterRequest;
import org.example.bankingapi.dto.response.AuthResponse;
import org.example.bankingapi.entity.BankAccount;
import org.example.bankingapi.entity.RefreshToken;
import org.example.bankingapi.entity.RevokedToken;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.Role;
import org.example.bankingapi.exception.DuplicateResourceException;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.BankAccountRepository;
import org.example.bankingapi.repository.RefreshTokenRepository;
import org.example.bankingapi.repository.RevokedTokenRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.security.JwtService;
import org.example.bankingapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        revokedTokenRepository.deleteByUsername(user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("[XÁC THỰC] Người dùng '{}' đăng nhập thành công với vai trò {}", user.getUsername(), user.getRole());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên người dùng đã tồn tại: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được đăng ký: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .role(Role.CUSTOMER)
                .transactionPin(passwordEncoder.encode(request.getTransactionPin()))
                .enabled(true)
                .isKyc(false)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("[XÁC THỰC] Khách hàng mới đã đăng ký: '{}'. Vui lòng hoàn tất KYC để mở tài khoản ngân hàng.", user.getUsername());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không tìm thấy hoặc không hợp lệ"));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return buildAuthResponse(newAccessToken, storedToken.getToken(), user);
    }

    @Override
    @Transactional
    public void logout(String token) {
        String username = jwtService.extractUsername(token);
        Date expiration = jwtService.extractExpiration(token);

        RevokedToken revokedToken = RevokedToken.builder()
                .token(token)
                .username(username)
                .expiresAt(expiration.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();

        revokedTokenRepository.save(revokedToken);

        userRepository.findByUsername(username).ifPresent(user ->
                refreshTokenRepository.deleteByUserId(user.getId())
        );

        log.info("[XÁC THỰC] Người dùng '{}' đã đăng xuất. Token đã bị thu hồi.", username);
    }

    private String generateAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        if (existingToken.isPresent()) {
            RefreshToken tokenToUpdate = existingToken.get();
            tokenToUpdate.setToken(tokenValue);
            tokenToUpdate.setExpiresAt(expiresAt);
            refreshTokenRepository.saveAndFlush(tokenToUpdate);
        } else {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.saveAndFlush(newRefreshToken);
        }

        return tokenValue;
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .role(user.getRole().name())
                .username(user.getUsername())
                .build();
    }
}