package org.example.bankingapi.dto.request;

import org.example.bankingapi.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KycReviewRequest {
    @NotNull(message = "Cần có thông tin xác thực")
    private KycStatus status;

    private String rejectionReason;
}
