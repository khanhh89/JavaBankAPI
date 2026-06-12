package org.example.bankingapi.controller;

import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final BankAccountService bankAccountService;

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> getBalance(
            @PathVariable Long id,
            Authentication authentication) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy số dư thành công",
                bankAccountService.getBalanceByAccountId(id, authentication.getName()),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> getAccountByNumber(
            @PathVariable String accountNumber) {
        AccountResponseDto account = bankAccountService.getAccountByNumber(accountNumber);
        account.setBalance(null);

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đã tìm thấy tài khoản",
                account,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}