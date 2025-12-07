package com.github.milomarten.taisha_rangers2.exception;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;

public class NotInParty extends RuntimeException implements LocalizedResponseException {
    public NotInParty() {
        super("Who are you???");
    }

    @Override
    public CommandResponse getLocalizedMessage(LocalizationFactory factory) {
        return factory.createResponse("errors.party.not-present")
                .ephemeral(true);
    }
}
