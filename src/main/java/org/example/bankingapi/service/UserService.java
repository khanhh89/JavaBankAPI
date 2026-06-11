package org.example.bankingapi.service;

import org.example.bankingapi.dto.request.ChangePinRequest;
import org.example.bankingapi.dto.request.UpdateUserRequest;
import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDto> getAllUsers(Role role, Pageable pageable);
    UserResponseDto getUserById(Long id);
    UserResponseDto updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    void changePin(String username, ChangePinRequest request);
}
