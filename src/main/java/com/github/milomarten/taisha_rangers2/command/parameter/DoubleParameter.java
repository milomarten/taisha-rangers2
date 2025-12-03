package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

/**
 * A ParameterInfo which extracts a double from the command usage.
 * A potential defect, due to Discord4J, doubles are used in the transmission through the system instead of
 * BigDecimals. A custom solution can be provided, if I decide it's useful.
 */
@Builder
public class DoubleParameter implements ParameterInfo<Double> {
    /**
     * Standard constant for a required double parameter
     */
    public static final DoubleParameter REQUIRED =
            DoubleParameter.builder().build();

    /**
     * Standard constant for an optional double parameter which defaults to 0
     */
    public static final DoubleParameter DEFAULT_ZERO =
            DoubleParameter.builder().defaultValue(0d).build();

    /**
     * The default double value. If null or unset, the parameter is considered REQUIRED.
     * NaN, positive or negative infinity, or positive or negative 0 are all valid defaultValues, and setting
     * them as the default will treat the parameter as OPTIONAL.
     */
    private Double defaultValue;

    /**
     * The minimum double value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no minimum.
     */
    private Double minValue;

    /**
     * The maximum double value, as enforced by Discord.
     * In the future, this may also be enforced code-side.
     * If null, there is no maximum.
     */
    private Double maxValue;

    @Override
    public Double convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsDouble(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .type(ApplicationCommandOption.Type.NUMBER.getValue())
                .required(defaultValue == null)
                .minValue(Possible.ofNullable(minValue))
                .maxValue(Possible.ofNullable(maxValue));
    }
}
