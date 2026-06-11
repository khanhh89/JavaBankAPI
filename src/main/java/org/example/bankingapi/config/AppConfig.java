package org.example.bankingapi.config;

import lombok.RequiredArgsConstructor;
import org.example.bankingapi.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsServiceImpl;
    }
}
