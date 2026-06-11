package org.example.bankingapi.controller;

import org.example.bankingapi.dto.request.KycReviewRequest;
import org.example.bankingapi.dto.request.UpdateUserRequest;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.KycResponseDto;
import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.enums.KycStatus;
import org.example.bankingapi.service.KycService;
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
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasRole('STAFF')")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;
    private final KycService kycService;

    @GetMapping("/customers")
    public ResponseEntity<ApiDataResponse<Page<UserResponseDto>>> listCustomers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách khách hàng thành công",
                userService.getAllUsers(org.example.bankingapi.enums.Role.CUSTOMER, pageable),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<ApiDataResponse<UserResponseDto>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật thông tin khách hàng thành công",
                userService.updateUser(id, request),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/kyc/pending")
    public ResponseEntity<ApiDataResponse<Page<KycResponseDto>>> getPendingKyc(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách KYC đang chờ duyệt thành công",
                kycService.getAllKyc(KycStatus.PENDING, pageable),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/kyc/{id}/review")
    public ResponseEntity<ApiDataResponse<KycResponseDto>> reviewKyc(
            @PathVariable Long id,
            @Valid @RequestBody KycReviewRequest request,
            Authentication authentication) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Duyệt hồ sơ KYC thành công",
                kycService.reviewKyc(id, request, authentication.getName()),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}