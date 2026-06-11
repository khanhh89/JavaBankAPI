package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.request.ChangePinRequest;
import org.example.bankingapi.dto.request.UpdateUserRequest;
import org.example.bankingapi.dto.response.UserResponseDto;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.Role;
import org.example.bankingapi.exception.DuplicateResourceException;
import org.example.bankingapi.exception.InvalidPinException;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Role role, Pageable pageable) {
        return userRepository.findAllProjected(role, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findByIdProjected(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email đã được sử dụng: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());

        userRepository.save(user);

        return userRepository.findByIdProjected(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng sau khi cập nhật với id: " + id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));
        
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("[QUẢN TRỊ] Người dùng '{}' đã bị vô hiệu hóa (xóa mềm).", user.getUsername());
    }

    @Override
    @Transactional
    public void changePin(String username, ChangePinRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPinException("Mật khẩu hiện tại không đúng");
        }

        user.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        userRepository.save(user);
        log.info("[XÁC THỰC] Người dùng '{}' đã thay đổi mã PIN giao dịch.", username);
    }
}