package com.github.milomarten.taisha_rangers2.exception;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;

public class NotDM extends RuntimeException implements LocalizedResponseException {
    public NotDM() {
        super("You're not the DM!");
    }

    @Override
    public CommandResponse getLocalizedMessage(LocalizationFactory factory) {
        return factory.createResponse("errors.session.no-access")
                .ephemeral(true);
    }
}
