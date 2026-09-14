package com.sushishop.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
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
    void shouldNotThrowWhenBrevoRequestFails() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError());

        mailService.sendVerificationEmail("anton@example.com", "token123");

        mockServer.verify();
    }
}
