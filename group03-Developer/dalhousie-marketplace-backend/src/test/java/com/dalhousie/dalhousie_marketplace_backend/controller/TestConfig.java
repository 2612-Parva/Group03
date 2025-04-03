package com.dalhousie.dalhousie_marketplace_backend.controller;

import com.dalhousie.dalhousie_marketplace_backend.service.AuthService;
import com.dalhousie.dalhousie_marketplace_backend.util.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestConfig {

    @Bean
    public AuthService authService(JwtUtil jwtUtil, JavaMailSender mailSender) {
        return new AuthService(jwtUtil) {
            @Override
            public void sendEmail(String to, String subject, String text) {
                // Do nothing during tests
                System.out.println("[TEST] Email sending skipped: " + subject);
            }
        };
    }
}