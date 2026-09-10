package com.sushishop.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerProfileConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNestConnectionPropertiesUnderSpringNotSpringdoc() throws IOException {
        Resource resource = new ClassPathResource("application-docker.yml");
        var sources = new YamlPropertySourceLoader().load("application-docker", resource);
        PropertySource<?> source = sources.getFirst();
        Map<String, Object> properties = (Map<String, Object>) source.getSource();

        assertThat(properties)
                .as("datasource, redis, mail and oauth2 must live under spring.*, not springdoc.*")
                .containsKeys(
                        "spring.datasource.url",
                        "spring.data.redis.host",
                        "spring.mail.host",
                        "spring.security.oauth2.client.registration.google.client-id"
                )
                .doesNotContainKeys(
                        "springdoc.datasource.url",
                        "springdoc.data.redis.host",
                        "springdoc.mail.host"
                );

        assertThat(Objects.toString(properties.get("springdoc.api-docs.enabled"))).isEqualTo("true");
        assertThat(Objects.toString(properties.get("logging.level.org.springframework.cache"))).isEqualTo("WARN");
        assertThat(Objects.toString(properties.get("logging.level.org.springframework.data.redis"))).isEqualTo("WARN");
        assertThat(Objects.toString(properties.get("spring.jpa.show-sql"))).isEqualTo("false");
    }
}
