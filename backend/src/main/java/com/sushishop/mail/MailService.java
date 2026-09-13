package com.sushishop.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        sendStyledEmail(to, "Verify your Sushi Bas Shop account",
                "Welcome!",
                "Thanks for creating an account. Click the button below to verify your email address.",
                "Verify Email",
                baseUrl + "/verify-email?token=" + token);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        sendStyledEmail(to, "Reset your Sushi Bas Shop password",
                "Reset Password",
                "Click the button below to reset your password.",
                "Reset Password",
                baseUrl + "/reset-password?token=" + token);
    }

    private void sendStyledEmail(String to, String subject, String title, String body, String buttonText, String buttonUrl) {
        var message = (MimeMessagePreparator) mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText("""
                    <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                      <div style="background:#F97316;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                        <h1 style="color:#fff;margin:0;font-size:24px">Sushi Bas Shop</h1>
                      </div>
                      <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                        <h2 style="margin:0 0 12px;font-size:20px">%s</h2>
                        <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.5">%s</p>
                        <a href="%s" style="display:block;background:#F97316;color:#fff;text-align:center;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px">%s</a>
                        <p style="margin:24px 0 0;color:#9ca3af;font-size:13px">If you didn't request this, you can ignore this email.</p>
                      </div>
                    </div>
                    """.formatted(title, body, buttonUrl, buttonText), true);
        };

        try {
            mailSender.send(message);
            log.info("{} email sent to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send {} email to {}", subject, to, e);
        }
    }
}