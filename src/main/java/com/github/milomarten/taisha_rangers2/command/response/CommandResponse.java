package com.github.milomarten.taisha_rangers2.command.response;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * Encapsulate a way to respond to a command usage
 */
public interface CommandResponse {
    /**
     * Respond to the command usage event
     * @param event The context of the event
     * @return A Mono which completes whenever the action finished.
     */
    Mono<?> respond(ChatInputInteractionEvent event);

    /**
     * A standard Command Response, to reply to a message
     * @param message The message to say
     * @param ephemeral If true, the message is only visible to the user. If false, it is shown to everyone
     * @return The CommandResponse that replies in that way
     */
    static ReplyResponse reply(String message, boolean ephemeral) {
        return new ReplyResponse(message).ephemeral(ephemeral);
    }
}


