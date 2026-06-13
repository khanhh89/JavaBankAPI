package org.example.bankingapi.controller;

import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.service.BankAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountController accountController;

    @Test
    void getBalance_Success() {

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("123456789")
                .balance(BigDecimal.valueOf(1000000))
                .build();

        when(authentication.getName()).thenReturn("customer1");
        when(bankAccountService.getBalanceByAccountId(1L, "customer1"))
                .thenReturn(dto);

        ResponseEntity<ApiDataResponse<AccountResponseDto>> response =
                accountController.getBalance(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Lấy số dư thành công", response.getBody().getMessage());
        assertEquals("123456789",
                response.getBody().getData().getAccountNumber());

        verify(bankAccountService)
                .getBalanceByAccountId(1L, "customer1");
    }

    @Test
    void getAccountByNumber_Success() {

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("123456789")
                .balance(BigDecimal.valueOf(1000000))
                .build();

        when(bankAccountService.getAccountByNumber("123456789"))
                .thenReturn(dto);

        ResponseEntity<ApiDataResponse<AccountResponseDto>> response =
                accountController.getAccountByNumber("123456789");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Đã tìm thấy tài khoản",
                response.getBody().getMessage());
        assertEquals("123456789",
                response.getBody().getData().getAccountNumber());
    }

    @Test
    void getAccountByNumber_ShouldHideBalance() {

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("123456789")
                .balance(BigDecimal.valueOf(1000000))
                .build();

        when(bankAccountService.getAccountByNumber("123456789"))
                .thenReturn(dto);

        ResponseEntity<ApiDataResponse<AccountResponseDto>> response =
                accountController.getAccountByNumber("123456789");

        assertNull(response.getBody().getData().getBalance());
    }

    @Test
    void getBalance_ResponseStatus_ShouldBeOk() {

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("123456789")
                .build();

        when(authentication.getName()).thenReturn("customer1");
        when(bankAccountService.getBalanceByAccountId(anyLong(), anyString()))
                .thenReturn(dto);

        ResponseEntity<ApiDataResponse<AccountResponseDto>> response =
                accountController.getBalance(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAccountByNumber_ResponseStatus_ShouldBeOk() {

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("123456789")
                .build();

        when(bankAccountService.getAccountByNumber(anyString()))
                .thenReturn(dto);

        ResponseEntity<ApiDataResponse<AccountResponseDto>> response =
                accountController.getAccountByNumber("123456789");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
