package org.example.bankingapi.service.impl;

import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.entity.BankAccount;
import org.example.bankingapi.entity.User;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.BankAccountRepository;
import org.example.bankingapi.repository.UserRepository;
import org.example.bankingapi.util.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("customer1");
        user.setFullName("Nguyen Van A");

        account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber("123456789");
        account.setBalance(BigDecimal.valueOf(1000000));
        account.setUser(user);
    }

    @Test
    void createAccount_Success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(accountNumberGenerator.generate())
                .thenReturn("123456789");

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto result =
                bankAccountService.createAccount(1L);

        assertNotNull(result);
        assertEquals("123456789", result.getAccountNumber());

        verify(userRepository).findById(1L);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_UserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.createAccount(1L)
        );
    }

    @Test
    void getAccountById_Success() {

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountResponseDto result =
                bankAccountService.getAccountById(1L);

        assertEquals(1L, result.getId());
        assertEquals("123456789", result.getAccountNumber());
    }

    @Test
    void getAccountById_NotFound() {

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.getAccountById(1L)
        );
    }

    @Test
    void getAccountByNumber_Success() {

        when(bankAccountRepository.findByAccountNumber("123456789"))
                .thenReturn(Optional.of(account));

        AccountResponseDto result =
                bankAccountService.getAccountByNumber("123456789");

        assertEquals("123456789", result.getAccountNumber());
    }

    @Test
    void getAccountByNumber_NotFound() {

        when(bankAccountRepository.findByAccountNumber("123456789"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.getAccountByNumber("123456789")
        );
    }

    @Test
    void getAccountsByUserId_Success() {

        when(bankAccountRepository.findByUserId(1L))
                .thenReturn(List.of(account));

        List<AccountResponseDto> result =
                bankAccountService.getAccountsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("123456789",
                result.get(0).getAccountNumber());
    }

    @Test
    void getBalanceByAccountId_Success() {

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountResponseDto result =
                bankAccountService.getBalanceByAccountId(
                        1L,
                        "customer1"
                );

        assertEquals(
                BigDecimal.valueOf(1000000),
                result.getBalance()
        );
    }

    @Test
    void getBalanceByAccountId_AccountNotFound() {

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bankAccountService.getBalanceByAccountId(
                        1L,
                        "customer1"
                )
        );
    }

    @Test
    void getBalanceByAccountId_AccessDenied() {

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                AccessDeniedException.class,
                () -> bankAccountService.getBalanceByAccountId(
                        1L,
                        "anotherUser"
                )
        );
    }
}