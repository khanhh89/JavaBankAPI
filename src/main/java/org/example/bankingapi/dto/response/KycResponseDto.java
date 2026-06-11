package org.example.bankingapi.dto.response;

import org.example.bankingapi.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String documentType;
    private String documentNumber;
    private String frontImageUrl;
    private String backImageUrl;
    private KycStatus status;
    private String rejectionReason;
    private String reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
