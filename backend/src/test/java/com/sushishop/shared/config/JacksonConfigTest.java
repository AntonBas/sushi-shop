package com.sushishop.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConfigTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    private record Sample(String name, String description) {
    }

    @Test
    public void shouldTrimLeadingAndTrailingWhitespaceOnStringFields() throws IOException {
        Sample sample = objectMapper.readValue(
                "{\"name\":\"  Maki  \",\"description\":\"\\tSalmon roll\\n\"}", Sample.class);

        assertThat(sample.name()).isEqualTo("Maki");
        assertThat(sample.description()).isEqualTo("Salmon roll");
    }

    @Test
    public void shouldLeaveNullStringFieldsUnchanged() throws IOException {
        Sample sample = objectMapper.readValue("{\"name\":\"Maki\",\"description\":null}", Sample.class);

        assertThat(sample.description()).isNull();
    }
}
