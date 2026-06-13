package org.example.bankingapi.scheduler;

import org.example.bankingapi.repository.RefreshTokenRepository;
import org.example.bankingapi.repository.RevokedTokenRepository;
import org.example.bankingapi.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;

    @Scheduled(cron = "0 0 2 * * *") // Chạy lúc 2:00 AM mỗi ngày
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository.deleteByExpiresAtBefore(now);
        log.info("[SCHEDULER] Đã xoá refresh token hết hạn trước {}", now);

        revokedTokenRepository.deleteByExpiresAtBefore(now);
        log.info("[SCHEDULER] Đã xoá revoked token hết hạn trước {}", now);

        passwordResetOtpRepository.deleteByExpiresAtBefore(now);
        log.info("[SCHEDULER] Đã xoá OTP hết hạn trước {}", now);
    }
}
