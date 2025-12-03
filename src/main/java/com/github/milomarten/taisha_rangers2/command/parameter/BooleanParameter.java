package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import lombok.Builder;

import java.util.Optional;

/**
 * A ParameterInfo which extracts a boolean from the command usage
 */
@Builder
public class BooleanParameter implements ParameterInfo<Boolean> {
    /**
     * Standard constant for a required Boolean parameter
     */
    public static final BooleanParameter REQUIRED =
            BooleanParameter.builder().build();
    /**
     * Standard constant for an optional Boolean parameter which defaults to true
     */
    public static final BooleanParameter DEFAULT_TRUE =
            BooleanParameter.builder().defaultValue(true).build();
    /**
     * Standard constant for an optional Boolean parameter which defaults to false
     */
    public static final BooleanParameter DEFAULT_FALSE =
            BooleanParameter.builder().defaultValue(false).build();

    /**
     * The default boolean value. If null or unset, the parameter is considered REQUIRED.
     */
    private Boolean defaultValue;

    @Override
    public Boolean convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsBoolean(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                .required(defaultValue == null);
    }
}
