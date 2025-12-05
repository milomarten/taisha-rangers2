package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.response.ReplyResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.function.BiFunction;

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

    /**
     * Create a localized response which will be translated into the user's language.
     * This allows the caller to control how the message is resolved. This can be useful in particular for custom
     * formatting that the MessageSource does not natively handle, like Java 8 Time objects.
     * @param resolver A function which takes the MessageSource and Locale, to output a response message
     * @return A reply which will invoke the resolver to get the message.
     */
    public LocalizedDynamicReplyResponse createResponse(BiFunction<MessageSource, Locale, String> resolver) {
        return new LocalizedDynamicReplyResponse(this.messageSource, resolver);
    }

    @Override
    public LocalizedStrings localize(String key) {
        return createLocalizedString(key);
    }

    @Override
    public LocalizedStrings localize(String key, String suffix) {
        return localize(key + "." + suffix);
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

    public static class LocalizedDynamicReplyResponse extends ReplyResponse {
        private final MessageSource messageSource;
        private final BiFunction<MessageSource, Locale, String> func;

        private LocalizedDynamicReplyResponse(MessageSource messageSource, BiFunction<MessageSource, Locale, String> func) {
            super("");
            this.messageSource = messageSource;
            this.func = func;
        }

        @Override
        protected String getMessage(ChatInputInteractionEvent event) {
            var locale = event.getInteraction().getUserLocale();
            return func.apply(messageSource, DiscordLocales.fromDiscord(locale));
        }
    }
}
