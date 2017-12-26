package com.orshachar.knownissue.http.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An extension to jackson, to deserialize enums with case-insensitive strings
 */
public class JacksonEnumExtension {

    private static final Logger logger = LoggerFactory.getLogger(JacksonEnumExtension.class);

    public static SimpleModule createModule() {
        final SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new EnumBeanDeserializerModifier());
        module.addSerializer(Enum.class, new EnumBeanSerializer(Enum.class));
        return module;
    }

    private static class EnumBeanDeserializerModifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                                                             final JavaType type,
                                                             BeanDescription beanDesc,
                                                             final JsonDeserializer<?> deserializer) {
            return new EnumJsonDeserializer(type);

        }
    }

    private static class EnumJsonDeserializer extends JsonDeserializer<Enum> {
        private final Class<? extends Enum> enumClass;

        @SuppressWarnings("unchecked")
        public EnumJsonDeserializer(JavaType javaType) {
            this.enumClass = (Class<? extends Enum>) javaType.getRawClass();
        }

        public Enum deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext) throws IOException {
            try {
                return Enum.valueOf(enumClass, jsonParser.getValueAsString().toUpperCase());
            } catch (Exception e) {
                logger.error("Enum: {0}, isn't correct, please update fields to UPPER_CASE", enumClass);
                return Enum.valueOf(enumClass, jsonParser.getValueAsString().toLowerCase());
            }
        }
    }

    private static class EnumBeanSerializer extends StdSerializer<Enum> {
        public EnumBeanSerializer(Class<Enum> t) {
            super(t);
        }

        @Override
        public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.name().toLowerCase());
        }
    }
}
