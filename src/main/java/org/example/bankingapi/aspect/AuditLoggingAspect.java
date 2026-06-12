package org.example.bankingapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.bankingapi.dto.request.TransferRequest;
import org.example.bankingapi.dto.response.TransactionResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    // Ghi log thời gian thực hiện cho tất cả service methods
    @Around("execution(* org.example.bankingapi.service.impl.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[TIMING] {} hoàn thành trong {}ms lúc {}", method, elapsed, LocalDateTime.now());
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[TIMING] {} thất bại sau {}ms - Lỗi: {}", method, elapsed, ex.getMessage());
            throw ex;
        }
    }

    // Ghi log thời gian thực hiện cho tất cả controller methods
    @Around("execution(* org.example.bankingapi.controller.*.*(..))")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[TIMING][API] {} hoàn thành trong {}ms lúc {}", method, elapsed, LocalDateTime.now());
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[TIMING][API] {} thất bại sau {}ms - Lỗi: {}", method, elapsed, ex.getMessage());
            throw ex;
        }
    }

    // Audit log chuyển tiền - trước khi thực hiện
    @Before("execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))")
    public void logBeforeTransfer(JoinPoint joinPoint) {
        String currentUser = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.info("[KIỂM TOÁN] [{}] Người dùng '{}' bắt đầu chuyển tiền: từ TK {} đến TK {}, số tiền: {}",
                    LocalDateTime.now(), currentUser,
                    req.getFromAccountId(), req.getToAccountId(), req.getAmount());
        }
    }

    // Audit log chuyển tiền - thành công
    @AfterReturning(
            pointcut = "execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))",
            returning = "result"
    )
    public void logAfterTransfer(JoinPoint joinPoint, Object result) {
        if (result instanceof TransactionResponseDto dto) {
            log.info("[KIỂM TOÁN] [THÀNH CÔNG] TK {} đã chuyển {} đến TK {}. Mã tham chiếu: {}",
                    dto.getFromAccountNumber(), dto.getAmount(),
                    dto.getToAccountNumber(), dto.getReferenceCode());
        }
    }

    // Audit log chuyển tiền - thất bại
    @AfterThrowing(
            pointcut = "execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))",
            throwing = "exception"
    )
    public void logTransferFailure(JoinPoint joinPoint, Exception exception) {
        String currentUser = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.error("[KIỂM TOÁN] [THẤT BẠI] Người dùng '{}' chuyển tiền THẤT BẠI: từ TK {} đến TK {}, số tiền: {}. Lý do: {}",
                    currentUser, req.getFromAccountId(), req.getToAccountId(),
                    req.getAmount(), exception.getMessage());
        }
    }

    // Audit log duyệt KYC
    @AfterReturning(
            pointcut = "execution(* org.example.bankingapi.service.impl.KycServiceImpl.reviewKyc(..))",
            returning = "result"
    )
    public void logKycReview(JoinPoint joinPoint, Object result) {
        String reviewer = getCurrentUsername();
        log.info("[KIỂM TOÁN] KYC được thực hiện bởi '{}' lúc {}. Kết quả: {}",
                reviewer, LocalDateTime.now(), result);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "ẩn danh";
    }
}