package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.request.ForgotPasswordRequest;
import org.example.bankingapi.dto.request.ResetPasswordRequest;
import org.example.bankingapi.dto.request.VerifyOtpRequest;
import org.example.bankingapi.entity.PasswordResetOtp;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.PasswordResetOtpRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.service.EmailService;
import org.example.bankingapi.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${otp.expiration-minutes:5}")
    private long otpExpirationMinutes;

    // Bước 1: Gửi OTP về email
    @Override
    @Transactional
    public void sendOtp(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản với email: " + request.getEmail()));

        // Xoá OTP cũ
        otpRepository.deleteByEmail(request.getEmail());

        String otp = generateOtp();
        PasswordResetOtp entity = PasswordResetOtp.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .build();
        otpRepository.save(entity);

        emailService.sendOtpEmail(request.getEmail(), otp);
        log.info("[RESET MK] Đã gửi OTP đến email '{}'", request.getEmail());
    }

    // Bước 2: Xác minh OTP → trả về reset token
    @Override
    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        PasswordResetOtp entity = otpRepository
                .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "OTP không tồn tại. Vui lòng yêu cầu mã mới."));

        if (entity.isExpired()) {
            otpRepository.delete(entity);
            throw new IllegalArgumentException("OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        if (!entity.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("OTP không đúng. Vui lòng kiểm tra lại.");
        }

        // Gán reset token, TTL thêm 10 phút từ lúc verify
        String resetToken = UUID.randomUUID().toString();
        entity.setResetToken(resetToken);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(entity);

        log.info("[RESET MK] OTP hợp lệ cho email '{}'. Đã cấp reset token.", request.getEmail());
        return resetToken;
    }

    // Bước 3: Đặt lại mật khẩu bằng reset token
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        PasswordResetOtp entity = otpRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reset token không hợp lệ hoặc đã hết hạn."));

        if (entity.isExpired()) {
            otpRepository.delete(entity);
            throw new IllegalArgumentException("Reset token đã hết hạn. Vui lòng thực hiện lại từ đầu.");
        }

        User user = userRepository.findByEmail(entity.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.delete(entity);
        log.info("[RESET MK] User '{}' đã đặt lại mật khẩu thành công.", user.getUsername());
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
