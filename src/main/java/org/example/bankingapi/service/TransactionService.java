package org.example.bankingapi.service;

import org.example.bankingapi.dto.request.TransferRequest;
import org.example.bankingapi.dto.response.TransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponseDto transfer(TransferRequest request, String currentUsername);
    Page<TransactionResponseDto> getStatement(Long accountId, String currentUsername, Pageable pageable);
}
