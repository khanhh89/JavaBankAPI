package org.example.bankingapi.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}
