package org.example.bankingapi.util;

import org.example.bankingapi.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private final BankAccountRepository bankAccountRepository;

    public String generate() {
        String number;
        do {
            number = "1001" + String.format("%08d", (long) (Math.random() * 100_000_000L));
        } while (bankAccountRepository.existsByAccountNumber(number));
        return number;
    }
}
