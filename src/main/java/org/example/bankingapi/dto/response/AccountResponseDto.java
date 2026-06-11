package org.example.bankingapi.dto.response;

import org.example.bankingapi.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private Long userId;
    private String ownerName;
    private LocalDateTime createdAt;
}
