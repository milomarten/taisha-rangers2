package com.github.milomarten.taisha_rangers2.command.response;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReplyResponse implements CommandResponse {
    private final String message;
    private boolean ephemeral;
    private AllowedMentions allowedMentions;

    @Override
    public Mono<?> respond(ChatInputInteractionEvent event) {
        return event.reply(message)
                .withEphemeral(ephemeral)
                .withAllowedMentions(Possible.ofNullable(allowedMentions));
    }

    public ReplyResponse ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ReplyResponse allowedMentions(AllowedMentions allowedMentions) {
        this.allowedMentions = allowedMentions;
        return this;
    }
}
