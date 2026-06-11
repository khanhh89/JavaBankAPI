package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.entity.BankAccount;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.BankAccountRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountResponseDto createAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));

        BankAccount account = BankAccount.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .build();

        bankAccountRepository.save(account);
        log.info("[TÀI KHOẢN] Tài khoản mới được tạo cho người dùng '{}': {}", user.getUsername(), account.getAccountNumber());
        return mapToDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + id));
        return mapToDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByNumber(String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với số: " + accountNumber));
        return mapToDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        return bankAccountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getBalanceByAccountId(Long accountId, String currentUsername) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + accountId));

        // Security: verify ownership
        if (!account.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào tài khoản này");
        }

        return mapToDto(account);
    }

    public AccountResponseDto mapToDto(BankAccount account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .userId(account.getUser().getId())
                .ownerName(account.getUser().getFullName())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "1001" + String.format("%08d", (long) (Math.random() * 100_000_000L));
        } while (bankAccountRepository.existsByAccountNumber(number));
        return number;
    }
}