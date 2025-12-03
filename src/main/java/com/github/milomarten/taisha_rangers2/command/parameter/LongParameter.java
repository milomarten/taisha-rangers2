package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

/**
 * A ParameterInfo which extracts a long from the command usage.
 * Please keep in mind of potential exceptions if a too-large number is provided by the user. If using an LongParameter,
 * you should also consider enforcing lower and upper bounds.
 */
@Builder
public class LongParameter implements ParameterInfo<Long> {
    /**
     * Standard constant for a required long parameter.
     */
    public static final LongParameter REQUIRED =
            LongParameter.builder().build();

    /**
     * Standard constant for an optional long parameter that defaults to 0.
     */
    public static final LongParameter DEFAULT_ZERO =
            LongParameter.builder().defaultValue(0L).build();

    /**
     * The default long value. If null or unset, the parameter is considered REQUIRED.
     */
    private Long defaultValue;

    /**
     * The minimum long value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no minimum.
     */
    private Long minValue;

    /**
     * The maximum long value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no maximum.
     */
    private Long maxValue;

    @Override
    public Long convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue == null ? null : minValue.doubleValue()))
                .maxValue(Possible.ofNullable(maxValue == null ? null : maxValue.doubleValue()));
    }
}
