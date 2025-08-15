package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;

public interface ParameterParser<PARAM> {
    PARAM parse(ChatInputInteractionEvent event);

    List<ApplicationCommandOptionData> toDiscordSpec();
}
