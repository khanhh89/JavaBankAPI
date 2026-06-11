package org.example.bankingapi.service;

import org.example.bankingapi.dto.response.AccountResponseDto;

import java.util.List;

public interface BankAccountService {
    AccountResponseDto createAccount(Long userId);
    AccountResponseDto getAccountById(Long id);
    AccountResponseDto getAccountByNumber(String accountNumber);
    List<AccountResponseDto> getAccountsByUserId(Long userId);
    AccountResponseDto getBalanceByAccountId(Long accountId, String currentUsername);
}
