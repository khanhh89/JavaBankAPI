package org.example.bankingapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "Cần có ID tài khoản nguồn")
    private Long fromAccountId;

    @NotNull(message = "Cần có ID tài khoản mục tiêu")
    private Long toAccountId;

    @NotNull(message = "Số tiền cần thiết")
    @DecimalMin(value = "1000", message = "Bé nhất là 1,000")
    @DecimalMax(value = "500000000", message = "Lớn nhất là 500 triệu")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "Cần có mã pin")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã pin phải là 6 số")
    private String transactionPin;

    private String referenceCode;
}
