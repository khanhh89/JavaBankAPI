package org.example.bankingapi.controller;

import org.example.bankingapi.dto.request.TransferRequest;
import org.example.bankingapi.dto.response.ApiDataResponse;
import org.example.bankingapi.dto.response.TransactionResponseDto;
import org.example.bankingapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/transactions") // Thay đổi đường dẫn để khớp với ma trận
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiDataResponse<TransactionResponseDto>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Chuyển tiền thành công",
                transactionService.transfer(request, authentication.getName()),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/accounts/{accountId}/statement")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiDataResponse<Page<TransactionResponseDto>>> getStatement(
            @PathVariable Long accountId,
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy sao kê giao dịch thành công",
                transactionService.getStatement(accountId, authentication.getName(), pageable),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}