package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.exception.LocalizedResponseException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

public abstract class LocalizedCommandSpec<PARAM> extends CommandSpec<PARAM> {
    protected LocalizationFactory localizationFactory;

    public LocalizedCommandSpec(String id) {
        super(id, id);
    }

    @Autowired
    public void setLocalizationFactory(LocalizationFactory localizationFactory) {
        this.localizationFactory = localizationFactory;
        this.setLocalizer(localizationFactory.withPrefix("command"));
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
