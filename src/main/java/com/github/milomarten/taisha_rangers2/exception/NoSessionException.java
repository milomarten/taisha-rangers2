package com.github.milomarten.taisha_rangers2.exception;

import discord4j.common.util.Snowflake;

public class NoSessionException extends RuntimeException{
    private final Snowflake snowflake;

    public NoSessionException(Snowflake snowflake) {
        super("No session found???");
        this.snowflake = snowflake;
    }
}
