package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

@Builder
public class DoubleParameter implements ParameterInfo<Double> {
    public static final DoubleParameter REQUIRED =
            DoubleParameter.builder().build();
    public static final DoubleParameter DEFAULT_ZERO =
            DoubleParameter.builder().defaultValue(0d).build();

    private Double defaultValue;
    private Double minValue;
    private Double maxValue;

    @Override
    public Double convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsDouble(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .type(ApplicationCommandOption.Type.NUMBER.getValue())
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue))
                .maxValue(Possible.ofNullable(maxValue));
    }
}
