package org.example.bankingapi.service;

import org.example.bankingapi.dto.request.KycReviewRequest;
import org.example.bankingapi.dto.response.KycResponseDto;
import org.example.bankingapi.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface KycService {
    KycResponseDto uploadKyc(String username, String documentType, String documentNumber,
                              MultipartFile frontImage, MultipartFile backImage);
    KycResponseDto reviewKyc(Long kycId, KycReviewRequest request, String reviewerUsername);
    KycResponseDto getKycByUserId(Long userId);
    Page<KycResponseDto> getAllKyc(KycStatus status, Pageable pageable);
}
