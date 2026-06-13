package org.example.bankingapi.service;

import org.example.bankingapi.dto.request.ForgotPasswordRequest;
import org.example.bankingapi.dto.request.ResetPasswordRequest;
import org.example.bankingapi.dto.request.VerifyOtpRequest;

public interface PasswordResetService {
    void sendOtp(ForgotPasswordRequest request);
    String verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
}
