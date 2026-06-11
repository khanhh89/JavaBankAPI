package org.example.bankingapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

    @Before("execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))")
    public void logBeforeTransfer(JoinPoint joinPoint) {
        String currentUser = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.info("[KIỂM TOÁN] [{}] Người dùng '{}' bắt đầu chuyển tiền: từ tài khoản {} đến tài khoản {}, số tiền: {}",
                    LocalDateTime.now(), currentUser,
                    req.getFromAccountId(), req.getToAccountId(), req.getAmount());
        }
    }

    @AfterReturning(
            pointcut = "execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))",
            returning = "result"
    )
    public void logAfterTransfer(JoinPoint joinPoint, Object result) {
        if (result instanceof TransactionResponseDto dto) {
            log.info("[KIỂM TOÁN] [THÀNH CÔNG] Tài khoản {} đã chuyển {} đến Tài khoản {}. Mã tham chiếu: {}",
                    dto.getFromAccountNumber(),
                    dto.getAmount(),
                    dto.getToAccountNumber(),
                    dto.getReferenceCode());
        }
    }

    @AfterThrowing(
            pointcut = "execution(* org.example.bankingapi.service.impl.TransactionServiceImpl.transfer(..))",
            throwing = "exception"
    )
    public void logTransferFailure(JoinPoint joinPoint, Exception exception) {
        String currentUser = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.error("[KIỂM TOÁN] [THẤT BẠI] Người dùng '{}' chuyển tiền THẤT BẠI: từ tài khoản {} đến tài khoản {}, số tiền: {}. Lý do: {}",
                    currentUser,
                    req.getFromAccountId(), req.getToAccountId(),
                    req.getAmount(), exception.getMessage());
        }
    }

    @AfterReturning(
            pointcut = "execution(* org.example.bankingapi.service.impl.BankAccountServiceImpl.*(..)) && " +
                       "(execution(* *.credit(..)) || execution(* *.debit(..)))",
            returning = "result"
    )
    public void logBalanceChange(JoinPoint joinPoint, Object result) {
        log.info("[KIỂM TOÁN] Thao tác thay đổi số dư '{}' đã hoàn tất. Kết quả: {}",
                joinPoint.getSignature().getName(), result);
    }

    @AfterReturning(
            pointcut = "execution(* org.example.bankingapi.service.impl.KycServiceImpl.reviewKyc(..))",
            returning = "result"
    )
    public void logKycReview(JoinPoint joinPoint, Object result) {
        String reviewer = getCurrentUsername();
        log.info("[KIỂM TOÁN] KYC được thực hiện bởi '{}'. Kết quả: {}", reviewer, result);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "ẩn danh";
    }
}