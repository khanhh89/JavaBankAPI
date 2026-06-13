package org.example.bankingapi.service.impl;

import org.example.bankingapi.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[Banking App] Mã OTP đặt lại mật khẩu");
            helper.setText(buildHtmlTemplate(otp), true);

            mailSender.send(message);
            log.info("[EMAIL] Đã gửi OTP đến email: {}", toEmail);
        } catch (Exception e) {
            log.error("[EMAIL] Lỗi gửi email đến {}: {} - {}", toEmail, e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    private String buildHtmlTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: auto; background: #ffffff; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a73e8; text-align: center;">Banking App</h2>
                    <p style="color: #333;">Xin chào,</p>
                    <p style="color: #333;">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                       Vui lòng sử dụng mã OTP bên dưới:</p>
                    <div style="text-align: center; margin: 28px 0;">
                      <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px;
                                   color: #1a73e8; background: #e8f0fe; padding: 12px 24px;
                                   border-radius: 8px;">%s</span>
                    </div>
                    <p style="color: #e53935; font-size: 13px;">
                      ⚠️ Mã OTP có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này với bất kỳ ai.
                    </p>
                    <p style="color: #999; font-size: 12px;">
                      Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                    <p style="color: #bbb; font-size: 11px; text-align: center;">
                      © 2026 Banking App. Mọi quyền được bảo lưu.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(otp);
    }
}
