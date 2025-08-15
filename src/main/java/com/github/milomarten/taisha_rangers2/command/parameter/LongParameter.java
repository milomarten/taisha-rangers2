package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

@Builder
public class LongParameter implements ParameterInfo<Long> {
    public static final LongParameter REQUIRED =
            LongParameter.builder().build();
    public static final LongParameter DEFAULT_ZERO =
            LongParameter.builder().defaultValue(0L).build();

    private Long defaultValue;
    private Long minValue;
    private Long maxValue;

    @Override
    public Long convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue == null ? null : minValue.doubleValue()))
                .maxValue(Possible.ofNullable(maxValue == null ? null : maxValue.doubleValue()));
    }
}
