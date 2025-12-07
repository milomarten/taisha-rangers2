package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.exception.LocalizedResponseException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

public abstract class LocalizedCommandSpec<PARAM> extends CommandSpec<PARAM> {
    private final String id;

    protected LocalizationFactory localizationFactory;
    protected Localizer localizer;

    public LocalizedCommandSpec(String id) {
        this.id = id;
    }

    @Autowired
    public void setLocalizationFactory(LocalizationFactory localizationFactory) {
        this.localizationFactory = localizationFactory;
        this.localizer = localizationFactory.withPrefix("command");
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder decorate(ImmutableApplicationCommandRequest.Builder builder) {
        var localName = localizer.localize(id, "name");
        var localDescription = localizer.localize(id, "description");
        return builder
                .name(this.id)
                .nameLocalizationsOrNull(localName.getDiscordifiedTranslations())
                .description(localDescription.key())
                .descriptionLocalizationsOrNull(localDescription.getDiscordifiedTranslations())
                .addAllOptions(getParameterParser().toDiscordSpec(
                        localizer.withPrefix(id).withPrefix("parameter")
                ));
    }

    @Override
    protected Mono<Void> handleException(ChatInputInteractionEvent event, Throwable ex) {
        if (ex instanceof LocalizedResponseException localized) {
            return localized.getLocalizedMessage(localizationFactory)
                    .respond(event)
                    .then();
        } else {
            return localizationFactory.createResponse("errors.generic", ex.getClass().getSimpleName(), ex.getMessage())
                    .respond(event)
                    .then();
        }
    }
}
