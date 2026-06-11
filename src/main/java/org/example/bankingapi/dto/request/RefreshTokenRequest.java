package org.example.bankingapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Cần có Refresh Token")
    private String refreshToken;
}
