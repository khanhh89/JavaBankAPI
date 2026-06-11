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

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final BankAccountService bankAccountService;

    // API này nên được chuyển sang AdminController hoặc StaffController
    @PostMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> createAccount(@PathVariable Long userId) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tạo tài khoản thành công",
                bankAccountService.createAccount(userId),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

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

    // API này nên được chuyển sang AdminController hoặc StaffController
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> getAccountById(@PathVariable Long id) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin tài khoản thành công",
                bankAccountService.getAccountById(id),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // API này nên được chuyển sang AdminController hoặc StaffController
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<List<AccountResponseDto>>> getAccountsByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách tài khoản thành công",
                bankAccountService.getAccountsByUserId(userId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> getAccountByNumber(
            @PathVariable String accountNumber) {
        AccountResponseDto account = bankAccountService.getAccountByNumber(accountNumber);
        // Hide balance for privacy when looking up by number
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