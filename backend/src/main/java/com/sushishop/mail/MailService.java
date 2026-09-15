package com.sushishop.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 200;

    private final RestClient restClient;
    private final String fromEmail;
    private final String fromName;

    @Value("${app.frontend-url}")
    private String baseUrl;

    public MailService(@Value("${app.mail.api-key}") String apiKey,
                        @Value("${app.mail.from-email}") String fromEmail,
                        @Value("${app.mail.from-name}") String fromName) {
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3/smtp/email")
                .defaultHeader("api-key", apiKey)
                .build();
    }

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
        String html = """
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
                """.formatted(title, body, buttonUrl, buttonText);

        var payload = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", html
        );

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                restClient.post()
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("{} email sent to {}", subject, to);
                return;
            } catch (Exception e) {
                if (attempt == MAX_ATTEMPTS) {
                    log.error("Failed to send {} email to {} after {} attempts", subject, to, MAX_ATTEMPTS, e);
                    return;
                }
                log.warn("Attempt {}/{} failed to send {} email to {}, retrying", attempt, MAX_ATTEMPTS, subject, to, e);
                sleepBeforeRetry();
            }
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
