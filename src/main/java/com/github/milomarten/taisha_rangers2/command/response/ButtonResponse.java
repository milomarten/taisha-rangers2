package com.github.milomarten.taisha_rangers2.command.response;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * Encapsulate a way to respond to a command usage
 */
public interface ButtonResponse {
    /**
     * Respond to the command usage event
     * @param event The context of the event
     * @return A Mono which completes whenever the action finished.
     */
    Mono<?> respond(ButtonInteractionEvent event);
}


