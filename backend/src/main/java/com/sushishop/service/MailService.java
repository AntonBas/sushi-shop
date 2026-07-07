package com.sushishop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        var message = (MimeMessagePreparator) mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Verify your Sushi Bas Shop account");
            helper.setText("""
                    <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                      <div style="background:#F97316;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                        <h1 style="color:#fff;margin:0;font-size:24px">Sushi Bas Shop</h1>
                      </div>
                      <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                        <h2 style="margin:0 0 12px;font-size:20px">Welcome!</h2>
                        <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.5">
                          Thanks for creating an account. Click the button below to verify your email address.
                        </p>
                        <a href="%s/verify-email?token=%s"
                           style="display:block;background:#F97316;color:#fff;text-align:center;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px">
                          Verify Email
                        </a>
                        <p style="margin:24px 0 0;color:#9ca3af;font-size:13px">
                          If you didn't create this account, you can ignore this email.
                        </p>
                      </div>
                    </div>
                    """.formatted(baseUrl, token), true);
        };
        mailSender.send(message);
        log.info("Verification email sent to {}", to);
    }

    public void sendPasswordResetEmail(String to, String token) {
        var message = (MimeMessagePreparator) mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Reset your Sushi Bas Shop password");
            helper.setText("""
                    <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                      <div style="background:#F97316;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                        <h1 style="color:#fff;margin:0;font-size:24px">Sushi Bas Shop</h1>
                      </div>
                      <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                        <h2 style="margin:0 0 12px;font-size:20px">Reset Password</h2>
                        <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.5">
                          Click the button below to reset your password.
                        </p>
                        <a href="%s/reset-password?token=%s"
                           style="display:block;background:#F97316;color:#fff;text-align:center;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px">
                          Reset Password
                        </a>
                        <p style="margin:24px 0 0;color:#9ca3af;font-size:13px">
                          If you didn't request this, you can ignore this email.
                        </p>
                      </div>
                    </div>
                    """.formatted(baseUrl, token), true);
        };
        mailSender.send(message);
        log.info("Password reset email sent to {}", to);
    }
}