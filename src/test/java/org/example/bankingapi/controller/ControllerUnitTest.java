package org.example.bankingapi.controller;
import org.example.bankingapi.config.SecurityConfig;
import org.example.bankingapi.dto.request.LoginRequest;
import org.example.bankingapi.dto.response.AccountResponseDto;
import org.example.bankingapi.dto.response.AuthResponse;
import org.example.bankingapi.dto.response.TransactionResponseDto;
import org.example.bankingapi.enums.AccountStatus;
import org.example.bankingapi.enums.TransactionType;
import org.example.bankingapi.exception.ResourceNotFoundException;
import org.example.bankingapi.repository.RevokedTokenRepository;
import org.example.bankingapi.security.JwtAuthenticationFilter;
import org.example.bankingapi.security.JwtService;
import org.example.bankingapi.service.AuthService;
import org.example.bankingapi.service.BankAccountService;
import org.example.bankingapi.service.TransactionService;
import org.example.bankingapi.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, AccountController.class, TransactionController.class})
@Import(SecurityConfig.class)
class ControllerUnitTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean BankAccountService bankAccountService;
    @MockBean TransactionService transactionService;
    @MockBean JwtService jwtService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean RevokedTokenRepository revokedTokenRepository;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // Test 1: Đăng nhập thành công trả về 200 + accessToken
    @Test
    void login_ReturnsOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .tokenType("Bearer")
                .username("testuser")
                .role("CUSTOMER")
                .expiresIn(3600L)
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access_token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    // Test 2: Đăng nhập thiếu username trả về 400
    @Test
    void login_MissingUsername_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test 3: Lấy số dư thành công với CUSTOMER role
    @Test
    @WithMockUser(username = "testuser", authorities = "ROLE_CUSTOMER")
    void getBalance_ReturnsOk() throws Exception {
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("100100000001")
                .balance(BigDecimal.valueOf(5000000))
                .status(AccountStatus.ACTIVE)
                .userId(1L)
                .ownerName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        when(bankAccountService.getBalanceByAccountId(eq(1L), eq("testuser"))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/customer/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(5000000));
    }

    // Test 4: Lấy số dư sai role - trả về 403
    @Test
    @WithMockUser(username = "staffuser", authorities = "ROLE_STAFF")
    void getBalance_WrongRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/customer/accounts/1/balance"))
                .andExpect(status().isForbidden());
    }

    // Test 5: Tra cứu tài khoản theo số không tìm thấy - trả về 404
    @Test
    @WithMockUser(username = "testuser", authorities = "ROLE_CUSTOMER")
    void getAccountByNumber_NotFound_Returns404() throws Exception {
        when(bankAccountService.getAccountByNumber("999999999999"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy tài khoản"));

        mockMvc.perform(get("/api/v1/customer/accounts/number/999999999999"))
                .andExpect(status().isNotFound());
    }

    // Test 6: Xem sao kê giao dịch thành công
    @Test
    @WithMockUser(username = "testuser", authorities = "ROLE_CUSTOMER")
    void getStatement_ReturnsOk() throws Exception {
        TransactionResponseDto tx = TransactionResponseDto.builder()
                .id(1L)
                .fromAccountNumber("100100000001")
                .toAccountNumber("100100000002")
                .amount(BigDecimal.valueOf(500000))
                .referenceCode("ref-001")
                .transactionType(TransactionType.DEBIT)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.getStatement(eq(1L), eq("testuser"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tx)));

        mockMvc.perform(get("/api/v1/customer/transactions/accounts/1/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].referenceCode").value("ref-001"));
    }
}
