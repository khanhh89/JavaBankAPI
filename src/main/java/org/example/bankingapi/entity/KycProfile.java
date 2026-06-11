package org.example.bankingapi.entity;

import org.example.bankingapi.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String documentType; // CCCD, PASSPORT

    @Column(nullable = false)
    private String documentNumber;

    private String frontImageUrl;  // Cloudinary URL

    private String backImageUrl;   // Cloudinary URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    private String rejectionReason;

    private String reviewedBy; // Staff username who reviewed

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
