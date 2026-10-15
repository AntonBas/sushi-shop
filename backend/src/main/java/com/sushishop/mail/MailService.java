package com.sushishop.mail;

import com.sushishop.shared.service.TransactionCallbacks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MailService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;
    private final TaskExecutor taskExecutor;
    private final String fromEmail;
    private final String fromName;

    @Value("${app.frontend-url}")
    private String baseUrl;

    @Value("${app.mail.retry-base-delay-ms:500}")
    private long retryBaseDelayMillis;

    public MailService(@Value("${app.mail.api-key}") String apiKey,
                        @Value("${app.mail.from-email}") String fromEmail,
                        @Value("${app.mail.from-name}") String fromName,
                        @Qualifier("mailTaskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("https://api.brevo.com/v3/smtp/email")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendVerificationEmail(String to, String token) {
        dispatch(() -> sendStyledEmail(to, "Verify your Sushi Bas Shop account",
                "Welcome!",
                "Thanks for creating an account. Click the button below to verify your email address.",
                "Verify Email",
                baseUrl + "/verify-email?token=" + token));
    }

    public void sendPasswordResetEmail(String to, String token) {
        dispatch(() -> sendStyledEmail(to, "Reset your Sushi Bas Shop password",
                "Reset Password",
                "Click the button below to reset your password.",
                "Reset Password",
                baseUrl + "/reset-password?token=" + token));
    }

    public void sendPasswordChangedNotification(String to) {
        dispatch(() -> sendPlainEmail(to, "Your Sushi Bas Shop password was changed",
                "Password Changed",
                "Your account password was just changed. If you didn't do this, please reset your password immediately."));
    }

    public void sendEmailChangeVerification(String to, String token) {
        dispatch(() -> sendStyledEmail(to, "Confirm your new Sushi Bas Shop email",
                "Confirm Your New Email",
                "Click the button below to confirm this address as your new account email.",
                "Confirm Email",
                baseUrl + "/verify-email-change?token=" + token));
    }

    public void sendEmailChangeRequestedNotification(String to, String newEmail) {
        dispatch(() -> sendPlainEmail(to, "Your Sushi Bas Shop email change request",
                "Email Change Requested",
                "A request was made to change your account email to " + HtmlUtils.htmlEscape(newEmail)
                        + ". If this wasn't you, please contact support immediately."));
    }

    public void sendOrderConfirmation(String to, OrderConfirmationEmail order) {
        String subject = order.paidOnline()
                ? "Payment received for your Sushi Bas Shop order #" + order.orderId()
                : "Your Sushi Bas Shop order #" + order.orderId() + " is confirmed";
        dispatch(() -> deliver(to, subject, orderConfirmationHtml(order)));
    }

    private void dispatch(Runnable send) {
        TransactionCallbacks.afterCommit(() -> taskExecutor.execute(send));
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

        deliver(to, subject, html);
    }

    private void sendPlainEmail(String to, String subject, String title, String body) {
        String html = """
                <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                  <div style="background:#F97316;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                    <h1 style="color:#fff;margin:0;font-size:24px">Sushi Bas Shop</h1>
                  </div>
                  <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                    <h2 style="margin:0 0 12px;font-size:20px">%s</h2>
                    <p style="margin:0;color:#6b7280;font-size:15px;line-height:1.5">%s</p>
                  </div>
                </div>
                """.formatted(title, body);

        deliver(to, subject, html);
    }

    private String orderConfirmationHtml(OrderConfirmationEmail order) {
        String rows = order.lines().stream()
                .map(line -> """
                        <tr>
                          <td style="padding:8px 0;border-bottom:1px solid #e5e7eb">%s × %d</td>
                          <td style="padding:8px 0;border-bottom:1px solid #e5e7eb;text-align:right;white-space:nowrap">%s</td>
                        </tr>
                        """.formatted(HtmlUtils.htmlEscape(line.productName()), line.quantity(), formatAmount(line.subtotal())))
                .collect(Collectors.joining());
        String title = order.paidOnline() ? "Payment Received" : "Order Confirmed";
        String payment = order.paidOnline() ? "Paid online" : "Pay on delivery";

        return """
                <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                  <div style="background:#F97316;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                    <h1 style="color:#fff;margin:0;font-size:24px">Sushi Bas Shop</h1>
                  </div>
                  <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                    <h2 style="margin:0 0 12px;font-size:20px">%s</h2>
                    <p style="margin:0 0 16px;color:#6b7280;font-size:15px;line-height:1.5">Thanks, %s! We've received order #%d and started preparing it.</p>
                    <table style="width:100%%;border-collapse:collapse;font-size:15px">%s
                      <tr>
                        <td style="padding:12px 0 0;font-weight:600">Total</td>
                        <td style="padding:12px 0 0;font-weight:600;text-align:right;white-space:nowrap">%s</td>
                      </tr>
                    </table>
                    <p style="margin:16px 0 0;color:#6b7280;font-size:14px;line-height:1.5">%s<br>%s</p>
                    <a href="%s" style="display:block;margin-top:24px;background:#F97316;color:#fff;text-align:center;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px">View Order</a>
                  </div>
                </div>
                """.formatted(title, HtmlUtils.htmlEscape(order.customerName()), order.orderId(), rows,
                formatAmount(order.totalAmount()), HtmlUtils.htmlEscape(order.deliveryDetails()), payment,
                baseUrl + "/profile/orders");
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " ₴";
    }

    private void deliver(String to, String subject, String html) {
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
                if (!isRetryable(e)) {
                    log.error("Brevo rejected {} email to {}, not retrying", subject, to, e);
                    return;
                }
                if (attempt == MAX_ATTEMPTS) {
                    log.error("Failed to send {} email to {} after {} attempts", subject, to, MAX_ATTEMPTS, e);
                    return;
                }
                log.warn("Attempt {}/{} failed to send {} email to {}, retrying", attempt, MAX_ATTEMPTS, subject, to, e);
                sleepBeforeRetry(attempt);
            }
        }
    }

    private static boolean isRetryable(Exception exception) {
        if (exception instanceof RestClientResponseException responseException) {
            var status = responseException.getStatusCode();
            return status.is5xxServerError() || status.value() == HttpStatus.TOO_MANY_REQUESTS.value();
        }
        return true;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(retryBaseDelayMillis << (attempt - 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
