package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface CommandHandler {
    ApplicationCommandRequest toDiscordSpec();
    Mono<?> run(ChatInputInteractionEvent event);
}
