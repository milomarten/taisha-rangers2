package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

@Builder
public class IntParameter implements ParameterInfo<Integer> {
    public static final IntParameter REQUIRED =
            IntParameter.builder().build();
    public static final IntParameter DEFAULT_ZERO =
            IntParameter.builder().defaultValue(0).build();

    private Integer defaultValue;
    private Integer minValue;
    private Integer maxValue;

    @Override
    public Integer convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .map(Long::intValue)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue == null ? null : minValue.doubleValue()))
                .maxValue(Possible.ofNullable(maxValue == null ? null : maxValue.doubleValue()));
    }
}
