package org.example.bankingapi.repository;

import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // JPQL Constructor Projection - UC-02
    @Query("SELECT new org.example.bankingapi.dto.response.UserResponseDto(" +
           "u.id, u.username, u.email, u.fullName, u.phone, u.role, u.enabled, u.isKyc, u.createdAt) " +
           "FROM User u WHERE (:role IS NULL OR u.role = :role)")
    Page<UserResponseDto> findAllProjected(@Param("role") Role role, Pageable pageable);

    @Query("SELECT new org.example.bankingapi.dto.response.UserResponseDto(" +
           "u.id, u.username, u.email, u.fullName, u.phone, u.role, u.enabled, u.isKyc, u.createdAt) " +
           "FROM User u WHERE u.id = :id")
    Optional<UserResponseDto> findByIdProjected(@Param("id") Long id);
}
