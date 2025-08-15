package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.Builder;

import java.util.Optional;

@Builder
public class StringParameter implements ParameterInfo<String> {
    public static final StringParameter REQUIRED =
            StringParameter.builder().build();
    public static final StringParameter DEFAULT_EMPTY_STRING =
            StringParameter.builder().defaultValue("").build();

    private String defaultValue;
    private Integer minLength;
    private Integer maxLength;

    @Override
    public String convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsString(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        return builder
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .required(defaultValue == null)
                .minLength(Possible.ofNullable(minLength))
                .maxLength(Possible.ofNullable(maxLength));
    }
}
