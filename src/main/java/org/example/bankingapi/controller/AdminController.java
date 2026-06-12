package org.example.bankingapi.controller;

import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.enums.Role;
import org.example.bankingapi.service.BankAccountService;
import org.example.bankingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final BankAccountService bankAccountService;

    @GetMapping("/users")
    public ResponseEntity<ApiDataResponse<Page<UserResponseDto>>> listUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách người dùng thành công",
                userService.getAllUsers(role, pageable),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiDataResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin người dùng thành công",
                userService.getUserById(id),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Xóa người dùng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<ApiDataResponse<List<AccountResponseDto>>> getUserAccounts(@PathVariable Long userId) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách tài khoản thành công",
                bankAccountService.getAccountsByUserId(userId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/users/{userId}/accounts")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> createAccount(@PathVariable Long userId) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tạo tài khoản thành công",
                bankAccountService.createAccount(userId),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiDataResponse<AccountResponseDto>> getAccountById(@PathVariable Long id) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin tài khoản thành công",
                bankAccountService.getAccountById(id),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}