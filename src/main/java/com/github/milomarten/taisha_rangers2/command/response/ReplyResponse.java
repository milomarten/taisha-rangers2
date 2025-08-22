package com.github.milomarten.taisha_rangers2.command.response;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Encapsulates various options for replying to a user
 */
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

    /**
     * Control the allowed mentions that can be in the reply
     * For safety, even if a message contains a mention, the notification will only occur if it is on a list of allowed
     * mentions. This ensures that users can't abuse the bot's permissions by, say, making it say @here or @everyone.
     * Omitting a mention means that it will still be resolved (i.e. It will say @Taisha Rangers, not &lt;@&12345&gt;),
     * it only ensures the notification isn't sent out.
     * @param allowedMentions The mentions the bot can use in this reply
     * @return This ReplyResponse, for chaining
     */
    public ReplyResponse allowedMentions(AllowedMentions allowedMentions) {
        this.allowedMentions = allowedMentions;
        return this;
    }
}
