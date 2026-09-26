package com.sushishop.shared.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .addModule(new SimpleModule().addDeserializer(String.class, new TrimmingStringDeserializer()))
                .changeDefaultPropertyInclusion(inclusion -> JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
