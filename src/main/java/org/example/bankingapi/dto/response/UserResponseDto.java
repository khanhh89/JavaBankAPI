package org.example.bankingapi.dto.response;

import org.example.bankingapi.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private boolean enabled;
    private boolean isKyc;
    private LocalDateTime createdAt;
}
