package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

/**
 * A ParameterInfo which extracts an int from the command usage.
 * Please keep in mind of potential exceptions if a too-large number is provided by the user. If using an IntParameter,
 * you should also consider enforcing lower and upper bounds.
 */
@Builder
public class IntParameter implements ParameterInfo<Integer> {
    /**
     * Standard constant for a required int parameter.
     */
    public static final IntParameter REQUIRED =
            IntParameter.builder().build();
    /**
     * Standard constant for an optional int parameter that defaults to 0.
     */
    public static final IntParameter DEFAULT_ZERO =
            IntParameter.builder().defaultValue(0).build();

    /**
     * The default int value. If null or unset, the parameter is considered REQUIRED.
     */
    private Integer defaultValue;

    /**
     * The minimum int value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no minimum.
     */
    private Integer minValue;

    /**
     * The maximum int value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no minimum.
     */
    private Integer maxValue;

    @Override
    public Integer convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .map(Long::intValue)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue == null ? null : minValue.doubleValue()))
                .maxValue(Possible.ofNullable(maxValue == null ? null : maxValue.doubleValue()));
    }
}
