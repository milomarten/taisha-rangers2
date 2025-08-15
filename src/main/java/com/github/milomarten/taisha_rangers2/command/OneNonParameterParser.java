package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;
import java.util.function.Function;

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
