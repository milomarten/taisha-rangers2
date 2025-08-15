package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface CommandHandler {
    Mono<?> run(ChatInputInteractionEvent event);
}
