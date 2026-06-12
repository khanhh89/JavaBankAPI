package org.example.bankingapi.controller;

import org.example.bankingapi.dto.request.ChangePasswordRequest;
import org.example.bankingapi.dto.request.ChangePinRequest;
import org.example.bankingapi.dto.request.UpdateUserRequest;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.enums.Role;
import org.example.bankingapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách người dùng thành công",
                userService.getAllUsers(role, pageable),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin người dùng thành công",
                userService.getUserById(id),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiDataResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật người dùng thành công",
                userService.updateUser(id, request),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/me/password")
    public ResponseEntity<ApiDataResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(authentication.getName(), request);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Thay đổi mật khẩu thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/me/pin")
    public ResponseEntity<ApiDataResponse<Void>> changePin(
            @Valid @RequestBody ChangePinRequest request,
            Authentication authentication) {

        userService.changePin(authentication.getName(), request);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Thay đổi mã PIN giao dịch thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}