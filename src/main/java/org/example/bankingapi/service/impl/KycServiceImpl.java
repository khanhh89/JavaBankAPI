package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.request.KycReviewRequest;
import org.example.bankingapi.dto.response.KycResponseDto;
import org.example.bankingapi.entity.BankAccount;
import org.example.bankingapi.entity.KycProfile;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.KycStatus;
import org.example.bankingapi.exception.DuplicateResourceException;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.BankAccountRepository;
import org.example.bankingapi.repository.KycProfileRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.service.CloudinaryService;
import org.example.bankingapi.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public KycResponseDto uploadKyc(String username, String documentType, String documentNumber,
                                     MultipartFile frontImage, MultipartFile backImage) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        kycProfileRepository.findByUserId(user.getId()).ifPresent(existing -> {
            if (existing.getStatus() == KycStatus.CONFIRMED) {
                throw new DuplicateResourceException("KYC của người dùng này đã được xác minh");
            }
        });

        String frontUrl = cloudinaryService.uploadFile(frontImage, "kyc/front");
        String backUrl = cloudinaryService.uploadFile(backImage, "kyc/back");

        KycProfile kycProfile = kycProfileRepository.findByUserId(user.getId())
                .orElse(KycProfile.builder().user(user).build());

        kycProfile.setDocumentType(documentType.toUpperCase());
        kycProfile.setDocumentNumber(documentNumber);
        kycProfile.setFrontImageUrl(frontUrl);
        kycProfile.setBackImageUrl(backUrl);
        kycProfile.setStatus(KycStatus.PENDING);
        kycProfile.setRejectionReason(null);

        kycProfile = kycProfileRepository.save(kycProfile);
        log.info("[KYC] Người dùng '{}' đã tải lên tài liệu KYC. Trạng thái: ĐANG CHỜ", username);

        return mapToDto(kycProfile);
    }

    @Override
    @Transactional
    public KycResponseDto reviewKyc(Long kycId, KycReviewRequest request, String reviewerUsername) {
        KycProfile kycProfile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ KYC: " + kycId));

        if (kycProfile.getStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xem xét các hồ sơ KYC đang chờ xử lý");
        }

        if (request.getStatus() == KycStatus.REJECTED && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Cần có lý do từ chối khi từ chối KYC");
        }

        kycProfile.setStatus(request.getStatus());
        kycProfile.setReviewedBy(reviewerUsername);

        if (request.getStatus() == KycStatus.CONFIRMED) {
            User user = kycProfile.getUser();
            user.setKyc(true);
            userRepository.save(user);
            log.info("[KYC] KYC của người dùng '{}' đã được XÁC NHẬN bởi '{}'", user.getUsername(), reviewerUsername);

            boolean hasAccount = !bankAccountRepository.findByUserId(user.getId()).isEmpty();
            if (!hasAccount) {
                BankAccount account = BankAccount.builder()
                        .accountNumber(generateAccountNumber())
                        .user(user)
                        .build();
                bankAccountRepository.save(account);
                log.info("[TÀI KHOẢN] Tự động tạo tài khoản ngân hàng {} cho người dùng '{}' sau khi phê duyệt KYC.", account.getAccountNumber(), user.getUsername());
            }

        } else if (request.getStatus() == KycStatus.REJECTED) {
            kycProfile.setRejectionReason(request.getRejectionReason());
            log.info("[KYC] KYC id={} đã BỊ TỪ CHỐI bởi '{}'. Lý do: {}", kycId, reviewerUsername, request.getRejectionReason());
        }

        kycProfile = kycProfileRepository.save(kycProfile);
        return mapToDto(kycProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public KycResponseDto getKycByUserId(Long userId) {
        KycProfile kycProfile = kycProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ KYC cho người dùng: " + userId));
        return mapToDto(kycProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycResponseDto> getAllKyc(KycStatus status, Pageable pageable) {
        if (status != null) {
            return kycProfileRepository.findAllByStatus(status, pageable).map(this::mapToDto);
        }
        return kycProfileRepository.findAll(pageable).map(this::mapToDto);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "1001" + String.format("%08d", (long) (Math.random() * 100_000_000L));
        } while (bankAccountRepository.existsByAccountNumber(number));
        return number;
    }

    private KycResponseDto mapToDto(KycProfile kyc) {
        return KycResponseDto.builder()
                .id(kyc.getId())
                .userId(kyc.getUser().getId())
                .username(kyc.getUser().getUsername())
                .documentType(kyc.getDocumentType())
                .documentNumber(kyc.getDocumentNumber())
                .frontImageUrl(kyc.getFrontImageUrl())
                .backImageUrl(kyc.getBackImageUrl())
                .status(kyc.getStatus())
                .rejectionReason(kyc.getRejectionReason())
                .reviewedBy(kyc.getReviewedBy())
                .createdAt(kyc.getCreatedAt())
                .updatedAt(kyc.getUpdatedAt())
                .build();
    }
}