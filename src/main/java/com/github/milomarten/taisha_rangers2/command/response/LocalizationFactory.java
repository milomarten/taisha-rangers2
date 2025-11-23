package com.github.milomarten.taisha_rangers2.command.response;

import com.github.milomarten.taisha_rangers2.command.DiscordLocales;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LocalizationFactory {
    private final MessageSource messageSource;

    public LocalizedReplyResponse createResponse(String key, Object... args) {
        return new LocalizedReplyResponse(key, args, this.messageSource);
    }

    public static class LocalizedReplyResponse extends ReplyResponse {
        private final String key;
        private final Object[] args;
        private final MessageSource messageSource;

        public LocalizedReplyResponse(String key, Object[] args, MessageSource messageSource) {
            super("");
            this.key = key;
            this.args = args;
            this.messageSource = messageSource;
        }

        @Override
        protected String getMessage(ChatInputInteractionEvent event) {
            var locale = event.getInteraction().getUserLocale();
            return messageSource.getMessage(this.key, this.args, DiscordLocales.fromDiscord(locale));
        }
    }
}
