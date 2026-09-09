package com.sushishop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ActuatorSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldRejectPrometheusWithoutCredentials() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPrometheusWithValidCredentials() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").with(httpBasic("prometheus", "changeme")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectPrometheusWithWrongCredentials() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").with(httpBasic("prometheus", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowHealthWithoutCredentials() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected /actuator/health to be publicly accessible, got " + status);
                    }
                });
    }

    @Test
    void shouldDisableSwaggerUiOutsideDockerProfile() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDisableApiDocsOutsideDockerProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }
}
