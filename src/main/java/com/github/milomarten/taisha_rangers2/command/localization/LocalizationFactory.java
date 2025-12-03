package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.response.ReplyResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * A service which allows for making localizable Discord commands
 */
@RequiredArgsConstructor
@Service
public class LocalizationFactory implements Localizer {
    private final MessageSource messageSource;

    /**
     * Create a localizedString using the backing messageSource
     * Today, it just returns the raw default translation without any other locales. But in the future, maybe...
     * @param key The key of the message
     * @param args The args to pass into the message
     * @return A LocalizedString containing the message requested
     */
    public LocalizedStrings createLocalizedString(String key, Object... args) {
        return LocalizedStrings.of(messageSource.getMessage(key, args, Locale.ROOT));
    }

    /**
     * Create a localized response which will be translated into the user's language
     * The attached messageSource will be queried using the provided key, and any arguments.
     * @see MessageSource
     * @param key The translation key to search for
     * @param args Arguments to pass to the MessageSource
     * @return A reply which will respond to the user taking translation into account
     */
    public LocalizedReplyResponse createResponse(String key, Object... args) {
        return new LocalizedReplyResponse(key, args, this.messageSource);
    }

    @Override
    public LocalizedStrings localize(String key) {
        return createLocalizedString(key);
    }

    public static class LocalizedReplyResponse extends ReplyResponse {
        private final String key;
        private final Object[] args;
        private final MessageSource messageSource;

        private LocalizedReplyResponse(String key, Object[] args, MessageSource messageSource) {
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
