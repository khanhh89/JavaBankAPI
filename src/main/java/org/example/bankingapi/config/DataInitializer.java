package org.example.bankingapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.Role;
import org.example.bankingapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123456"))
                        .email("admin@rikkeibank.vn")
                        .fullName("Quản trị viên hệ thống")
                        .role(Role.ADMIN)
                        .transactionPin(passwordEncoder.encode("000000"))
                        .enabled(true)
                        .isKyc(true)
                        .build();
                userRepository.save(admin);
                log.info("[KHỞI TẠO] Đã tạo tài khoản admin mặc định: admin / Admin@123456");
            }

            if (!userRepository.existsByUsername("staff01")) {
                User staff = User.builder()
                        .username("staff01")
                        .password(passwordEncoder.encode("Staff@123456"))
                        .email("staff01@rikkeibank.vn")
                        .fullName("Nhân viên ngân hàng 01")
                        .role(Role.STAFF)
                        .transactionPin(passwordEncoder.encode("111111"))
                        .enabled(true)
                        .isKyc(true)
                        .build();
                userRepository.save(staff);
                log.info("[KHỞI TẠO] Đã tạo tài khoản nhân viên mặc định: staff01 / Staff@123456");
            }
        };
    }
}