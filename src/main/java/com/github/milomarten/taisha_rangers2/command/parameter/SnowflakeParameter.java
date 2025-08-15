package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Builder
public class SnowflakeParameter implements ParameterInfo<Snowflake> {
    public static final SnowflakeParameter REQUIRED =
            SnowflakeParameter.builder().build();

    private final SnowflakeType type;
    private Snowflake defaultValue;

    @Override
    public Snowflake convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsSnowflake(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .type(this.type.value)
                .required(defaultValue == null);
    }

    @RequiredArgsConstructor
    public enum SnowflakeType {
        USER(ApplicationCommandOption.Type.USER.getValue()),
        ROLE(ApplicationCommandOption.Type.ROLE.getValue()),
        CHANNEL(ApplicationCommandOption.Type.CHANNEL.getValue()),
        MENTIONABLE(ApplicationCommandOption.Type.MENTIONABLE.getValue()),;

        private final int value;
    }
}
