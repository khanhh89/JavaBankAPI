package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.request.TransferRequest;
import org.example.bankingapi.dto.response.TransactionResponseDto;
import org.example.bankingapi.entity.BankAccount;
import org.example.bankingapi.entity.Transaction;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.enums.AccountStatus;
import org.example.bankingapi.enums.TransactionType;
import org.example.bankingapi.exception.DuplicateResourceException;
import org.example.bankingapi.exception.InsufficientBalanceException;
import org.example.bankingapi.exception.InvalidPinException;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.BankAccountRepository;
import org.example.bankingapi.repository.TransactionRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponseDto transfer(TransferRequest request, String currentUsername) {

        String refCode = request.getReferenceCode() != null
                ? request.getReferenceCode()
                : UUID.randomUUID().toString();

        if (transactionRepository.existsByReferenceCode(refCode)) {
            throw new DuplicateResourceException("Giao dịch trùng lặp: mã tham chiếu đã được xử lý");
        }

        BankAccount fromAccount = bankAccountRepository.findByIdWithLock(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nguồn"));

        if (!fromAccount.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Bạn không sở hữu tài khoản nguồn");
        }

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản nguồn không hoạt động");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getTransactionPin(), user.getTransactionPin())) {
            throw new InvalidPinException("Mã PIN giao dịch không hợp lệ");
        }

        BankAccount toAccount = bankAccountRepository.findByIdWithLock(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đích"));

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản đích không hoạt động");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Không thể chuyển tiền vào cùng một tài khoản");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Số dư không đủ. Hiện có: %s, Yêu cầu: %s",
                            fromAccount.getBalance(), request.getAmount()));
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .description(request.getDescription())
                .referenceCode(refCode)
                .build();

        transaction = transactionRepository.save(transaction);

        return mapToDto(transaction, fromAccount.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getStatement(Long accountId, String currentUsername, Pageable pageable) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + accountId));

        if (!account.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Bạn không sở hữu tài khoản này");
        }

        return transactionRepository.findAllByAccountId(accountId, pageable)
                .map(tx -> mapToDto(tx, accountId));
    }

    private TransactionResponseDto mapToDto(Transaction tx, Long viewerAccountId) {
        TransactionType type = tx.getFromAccount().getId().equals(viewerAccountId)
                ? TransactionType.DEBIT
                : TransactionType.CREDIT;

        return TransactionResponseDto.builder()
                .id(tx.getId())
                .fromAccountNumber(tx.getFromAccount().getAccountNumber())
                .toAccountNumber(tx.getToAccount().getAccountNumber())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .referenceCode(tx.getReferenceCode())
                .transactionType(type)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}