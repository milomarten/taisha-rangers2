package com.github.milomarten.taisha_rangers2.exception;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.common.util.Snowflake;

public class SessionAlreadyExistsException extends RuntimeException implements LocalizedResponseException {
    private final Snowflake snowflake;

    public SessionAlreadyExistsException(Snowflake snowflake) {
        super("Session already exists. Cancel the original one first!");
        this.snowflake = snowflake;
    }

    @Override
    public CommandResponse getLocalizedMessage(LocalizationFactory factory) {
        return factory.createResponse("errors.session.already-exists")
                .ephemeral(true);
    }
}
