package org.example.bankingapi.repository;

import org.example.bankingapi.entity.KycProfile;
import org.example.bankingapi.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByUserId(Long userId);

    Page<KycProfile> findAllByStatus(KycStatus status, Pageable pageable);
}
