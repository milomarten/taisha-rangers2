package com.github.milomarten.taisha_rangers2.config;

import discord4j.common.util.Snowflake;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public class DatabaseConverters {
    @Converter(autoApply = true)
    public static class ToSnowflake implements AttributeConverter<Snowflake, Long> {
        @Override
        public Long convertToDatabaseColumn(Snowflake attribute) {
            return attribute.asLong();
        }

        @Override
        public Snowflake convertToEntityAttribute(Long dbData) {
            return Snowflake.of(dbData);
        }
    }
}
