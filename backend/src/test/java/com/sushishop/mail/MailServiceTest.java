package com.sushishop.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MailServiceTest {

    private MockRestServiceServer mockServer;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService("test-api-key", "from@sushibas.shop", "Sushi Bas Shop");
        ReflectionTestUtils.setField(mailService, "baseUrl", "http://localhost:5173");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3/smtp/email")
                .defaultHeader("api-key", "test-api-key");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(mailService, "restClient", builder.build());
    }

    @Test
    void shouldSendVerificationEmailViaBrevoApi() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(POST))
                .andExpect(header("api-key", "test-api-key"))
                .andExpect(jsonPath("$.sender.email").value("from@sushibas.shop"))
                .andExpect(jsonPath("$.to[0].email").value("anton@example.com"))
                .andExpect(jsonPath("$.subject").value("Verify your Sushi Bas Shop account"))
                .andExpect(jsonPath("$.htmlContent").exists())
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendVerificationEmail("anton@example.com", "token123");

        mockServer.verify();
    }

    @Test
    void shouldSendPasswordResetEmailWithResetLink() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(jsonPath("$.htmlContent", containsString(
                        "http://localhost:5173/reset-password?token=token456")))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendPasswordResetEmail("anton@example.com", "token456");

        mockServer.verify();
    }

    @Test
    void shouldSendPasswordChangedNotificationWithoutActionLink() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(jsonPath("$.subject").value("Your Sushi Bas Shop password was changed"))
                .andExpect(jsonPath("$.htmlContent", containsString("password was just changed")))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendPasswordChangedNotification("anton@example.com");

        mockServer.verify();
    }

    @Test
    void shouldSendEmailChangeVerificationWithConfirmLink() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(jsonPath("$.to[0].email").value("new@example.com"))
                .andExpect(jsonPath("$.htmlContent", containsString(
                        "http://localhost:5173/verify-email-change?token=token789")))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendEmailChangeVerification("new@example.com", "token789");

        mockServer.verify();
    }

    @Test
    void shouldSendEmailChangeRequestedNotificationToOldAddress() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(jsonPath("$.to[0].email").value("old@example.com"))
                .andExpect(jsonPath("$.htmlContent", containsString("new@example.com")))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendEmailChangeRequestedNotification("old@example.com", "new@example.com");

        mockServer.verify();
    }

    @Test
    void shouldSendGoogleSignInDisabledNotification() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(jsonPath("$.to[0].email").value("anton@example.com"))
                .andExpect(jsonPath("$.subject").value("Google sign-in disabled for your Sushi Bas Shop account"))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendGoogleSignInDisabledNotification("anton@example.com");

        mockServer.verify();
    }

    @Test
    void shouldRetryUpToThreeTimesThenGiveUpWithoutThrowing() {
        mockServer.expect(ExpectedCount.times(3), requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError());

        mailService.sendVerificationEmail("anton@example.com", "token123");

        mockServer.verify();
    }

    @Test
    void shouldSendSuccessfullyAfterATransientFailure() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError());
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        mailService.sendVerificationEmail("anton@example.com", "token123");

        mockServer.verify();
    }
}
