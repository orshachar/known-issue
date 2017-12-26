package com.orshachar.knownissue.http.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {
    private static final ObjectMapper objectMapper = buildMapper();

    /**
     * Build an object mapper with our common configuration between server, client and tests.
     *
     * @return
     */
    private static ObjectMapper buildMapper() {
        return new ObjectMapper()
                //.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
                .enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(JacksonEnumExtension.createModule())
                ;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
