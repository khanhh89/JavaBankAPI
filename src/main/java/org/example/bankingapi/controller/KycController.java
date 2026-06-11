package org.example.bankingapi.controller;

import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.KycResponseDto;
import org.example.bankingapi.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customer/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiDataResponse<KycResponseDto>> uploadKyc(
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage,
            Authentication authentication) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tải lên hồ sơ KYC thành công. Đang chờ duyệt.",
                kycService.uploadKyc(authentication.getName(), documentType, documentNumber, frontImage, backImage),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiDataResponse<KycResponseDto>> getMyKyc(Authentication authentication) {
        // This endpoint should ideally call a method like getKycByUsername
        // For now, let's assume the service handles the username->userId logic
        // KycResponseDto kyc = kycService.getKycByUsername(authentication.getName());
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin KYC của bạn thành công.",
                null, // Replace with actual service call
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}