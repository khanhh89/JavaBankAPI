package org.example.bankingapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String firstMessage = errors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ");

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Yêu cầu không hợp lệ")
                .message(firstMessage)
                .path(request.getRequestURI())
                .details(errors)
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(401, "Không được phép", "Tên đăng nhập hoặc mật khẩu không đúng", request));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(403, "Bị cấm", "Tài khoản đã bị vô hiệu hóa", request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(403, "Bị cấm", "Bạn không có quyền truy cập tài nguyên này", request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(404, "Không tìm thấy", ex.getMessage(), request));
    }

    @ExceptionHandler({InsufficientBalanceException.class, DuplicateResourceException.class})
    public ResponseEntity<ErrorResponse> handleConflict(
            RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(409, "Xung đột", ex.getMessage(), request));
    }

    @ExceptionHandler({InvalidPinException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(
            RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "Yêu cầu không hợp lệ", ex.getMessage(), request));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError(400, "Yêu cầu không hợp lệ", "Kích thước tệp vượt quá giới hạn tối đa 5MB", request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Lỗi không mong muốn: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn", request));
    }

    private ErrorResponse buildError(int status, String error, String message, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}