package com.github.milomarten.taisha_rangers2.exception;

import discord4j.common.util.Snowflake;

public class SessionAlreadyExistsException extends RuntimeException{
    private final Snowflake snowflake;

    public SessionAlreadyExistsException(Snowflake snowflake) {
        super("Session already exists. Cancel the original one first!");
        this.snowflake = snowflake;
    }
}
