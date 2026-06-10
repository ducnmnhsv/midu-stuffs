package com.techx.tradex.common.utils.kotlin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class KotlinJackson {
    public static <T> T readValue(String input, ObjectMapper objectMapper, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(input, typeReference);
    }
    public static <T> T readValue(File f, ObjectMapper objectMapper, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(f, typeReference);
    }
}
