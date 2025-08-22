package com.github.milomarten.taisha_rangers2.command.parameters;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;
import java.util.function.Function;

/**
 * A ParameterParser for a command with no parameters, but uses the command usage event for some information
 * @param func A function which extracts some information from the command usage event.
 * @param <PARAM> The type of the parameter
 */
public record OneNonParameterParser<PARAM>(
        Function<ChatInputInteractionEvent, PARAM> func
) implements ParameterParser<PARAM> {
    @Override
    public PARAM parse(ChatInputInteractionEvent event) {
        return func.apply(event);
    }

    @Override
    public List<ApplicationCommandOptionData> toDiscordSpec() {
        return List.of();
    }
}
