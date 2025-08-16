package com.github.milomarten.taisha_rangers2.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DiscordModule extends SimpleModule {
    public DiscordModule() {
        super("discord-module");
        addSerializer(Snowflake.class, new SnowflakeSerializer());
        addDeserializer(Snowflake.class, new SnowflakeDeserializer());
        addKeySerializer(Snowflake.class, new SnowflakeKeySerializer());
        addKeyDeserializer(Snowflake.class, new SnowflakeKeyDeserializer());
    }

    public static class SnowflakeSerializer extends JsonSerializer<Snowflake> {
        @Override
        public void serialize(Snowflake value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.asLong());
        }
    }
    public static class SnowflakeDeserializer extends JsonDeserializer<Snowflake> {
        @Override
        public Snowflake deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return Snowflake.of(p.getValueAsLong());
        }
    }
    public static class SnowflakeKeySerializer extends JsonSerializer<Snowflake> {
        @Override
        public void serialize(Snowflake value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeFieldName(value.asString());
        }
    }
    public static class SnowflakeKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return Snowflake.of(key);
        }
    }
}
