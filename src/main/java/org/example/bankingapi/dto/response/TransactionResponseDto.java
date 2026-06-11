package org.example.bankingapi.dto.response;

import org.example.bankingapi.enums.TransactionType;
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
public class TransactionResponseDto {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
    private String referenceCode;
    private TransactionType transactionType;
    private LocalDateTime createdAt;
}
