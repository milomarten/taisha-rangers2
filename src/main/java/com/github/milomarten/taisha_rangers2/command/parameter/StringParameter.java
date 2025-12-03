package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

/**
 * A ParameterInfo which extracts a string from the command usage
 */
@Builder
public class StringParameter implements ParameterInfo<String> {
    /**
     * A standard constant for a required string parameter.
     */
    public static final StringParameter REQUIRED =
            StringParameter.builder().build();
    /**
     * A standard constant for an optional string parameter that defaults to empty string
     */
    public static final StringParameter DEFAULT_EMPTY_STRING =
            StringParameter.builder().defaultValue("").build();

    /**
     * The default String value. If null or unset, the parameter is considered REQUIRED.
     */
    private String defaultValue;

    /**
     * The minimum length of the string, as enforced by Discord
     * In the future, this may also be enforced code-side.
     * If null or unset, there is no minimum length
     */
    private Integer minLength;

    /**
     * The maximum length of the string, as enforced by Discord
     * In the future, this may also be enforced code-side
     * If null or unset, there is no maximum length
     */
    private Integer maxLength;

    @Override
    public String convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsString(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(defaultValue == null)
                .minLength(Possible.ofNullable(minLength))
                .maxLength(Possible.ofNullable(maxLength));
    }
}
