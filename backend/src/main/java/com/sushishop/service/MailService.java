package com.sushishop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verification Email");
        message.setText("Click to verify: " + baseUrl + "/api/auth/verify?token=" + token);
        mailSender.send(message);
        log.info("Verification email sent to {}", to);
    }

    public void sendPasswordResetEmail(String to, String token) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Email");
        message.setText("Click to reset your password: " + baseUrl + "/api/auth/reset?token=" + token);
        mailSender.send(message);
        log.info("Password reset email sent to {}", to);
    }
}
