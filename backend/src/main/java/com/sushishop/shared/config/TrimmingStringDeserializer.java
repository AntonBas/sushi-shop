package com.sushishop.shared.config;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public class TrimmingStringDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = super.deserialize(p, ctxt);
        return value == null ? null : value.strip();
    }
}
