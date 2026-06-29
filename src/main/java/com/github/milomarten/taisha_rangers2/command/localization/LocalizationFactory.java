package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.command.response.ReplyResponse;
import com.github.milomarten.taisha_rangers2.config.LocalizedDiscordService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.function.BiFunction;

/**
 * A service which allows for making localizable Discord commands
 */
@RequiredArgsConstructor
@Service
public class LocalizationFactory implements Localizer {
    @Getter private final MessageSource messageSource;

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
     * The output of the resolver will be returned as the message, with no other configuration supported. For more
     * advanced configuration of the final output, see createComplexResponse.
     * @param resolver A function which takes the MessageSource and Locale, to output a response message
     * @return A reply which will invoke the resolver to get the message.
     */
    public LocalizedDynamicReplyResponse createResponse(BiFunction<MessageSource, Locale, String> resolver) {
        return new LocalizedDynamicReplyResponse(this.messageSource, (ms, locale) -> {
            var msg = resolver.apply(ms, locale);
            return new ReplyResponse(msg);
        });
    }

    /**
     * Create a localized response which will be translated into the user's language.
     * This allows the caller to control how the message is resolved. This can be useful in particular for custom
     * formatting that the MessageSource does not natively handle, like Java 8 Time objects.
     * Unlike createResponse, this function can craft a fully-formed ReplyResponse to be sent on to the user, allowing
     * for translations of several dynamic components.
     * @param resolver A function which takes the MessageSource and Locale, to output a response message
     * @return A reply which will invoke the resolver to get the message.
     */
    public LocalizedDynamicReplyResponse createComplexResponse(BiFunction<MessageSource, Locale, ReplyResponse> resolver) {
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
        protected String getMessage(DeferrableInteractionEvent event) {
            return getMessage(event.getInteraction().getUserLocale());
        }

        public String getMessage(String locale) {
            return messageSource.getMessage(this.key, this.args, DiscordLocales.fromDiscord(locale));
        }
    }

    public static class LocalizedDynamicReplyResponse implements CommandResponse {
        private final MessageSource messageSource;
        private final BiFunction<MessageSource, Locale, ReplyResponse> func;

        private LocalizedDynamicReplyResponse(MessageSource messageSource, BiFunction<MessageSource, Locale, ReplyResponse> func) {
            this.messageSource = messageSource;
            this.func = func;
        }

        @Override
        public Mono<?> respond(DeferrableInteractionEvent event) {
            return func.apply(this.messageSource, DiscordLocales.fromDiscord(event.getInteraction().getUserLocale()))
                    .respond(event);
        }

        public ReplyResponse resolve(Locale locale) {
            return func.apply(this.messageSource, locale);
        }
    }
}
