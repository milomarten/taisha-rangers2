package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import lombok.Builder;

import java.util.Optional;

@Builder
public class BooleanParameter implements ParameterInfo<Boolean> {
    public static final BooleanParameter REQUIRED =
            BooleanParameter.builder().build();
    public static final BooleanParameter DEFAULT_TRUE =
            BooleanParameter.builder().defaultValue(true).build();
    public static final BooleanParameter DEFAULT_FALSE =
            BooleanParameter.builder().defaultValue(false).build();

    private Boolean defaultValue;

    @Override
    public Boolean convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsBoolean(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                .required(defaultValue == null);
    }
}
