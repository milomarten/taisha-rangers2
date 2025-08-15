package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(staticName = "create")
public class NoParameterParser<T> implements ParameterParser<T> {
    @Override
    public T parse(ChatInputInteractionEvent event) {
        return null;
    }

    @Override
    public List<ApplicationCommandOptionData> toDiscordSpec() {
        return List.of();
    }
}
