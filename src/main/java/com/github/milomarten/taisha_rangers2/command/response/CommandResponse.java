package com.github.milomarten.taisha_rangers2.command.response;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface CommandResponse {
    Mono<?> respond(ChatInputInteractionEvent event);

    static ReplyResponse reply(String message, boolean ephemeral) {
        return new ReplyResponse(message).ephemeral(ephemeral);
    }
}


