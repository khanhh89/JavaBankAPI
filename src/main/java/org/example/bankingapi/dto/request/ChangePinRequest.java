package org.example.bankingapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank(message = "Cần có mã PIN mới")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã PIN phải có chính xác 6 chữ số")
    private String newPin;
}
