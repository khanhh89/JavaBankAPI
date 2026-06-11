package org.example.bankingapi.repository;

import org.example.bankingapi.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByToken(String token);

    @Modifying
    @Transactional
    void deleteByUsername(String username);
}