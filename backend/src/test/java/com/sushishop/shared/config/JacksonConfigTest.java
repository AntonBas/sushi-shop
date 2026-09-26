package com.sushishop.shared.config;

import com.sushishop.auth.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class JacksonConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldTrimStringFieldsInHttpRequestBody() throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"  a@b.com\\t\"}"))
                .andExpect(status().isOk());

        verify(passwordResetService).forgotPassword("a@b.com");
    }

    @Test
    public void shouldSerializeErrorStatusByEnumName() throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    public void shouldWriteLocalDateTimeAsUtcAndReadUtcInputBack() {
        var dateTime = LocalDateTime.of(2026, 9, 27, 15, 30);

        assertThat(jsonMapper.writeValueAsString(dateTime)).isEqualTo("\"2026-09-27T15:30:00Z\"");
        assertThat(jsonMapper.readValue("\"2026-09-27T15:30:00.000Z\"", LocalDateTime.class)).isEqualTo(dateTime);
    }
}
