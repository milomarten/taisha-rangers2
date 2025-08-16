package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;

public interface NextSessionListener {
    default void setNextSessionManager(NextSessionManager manager) {}
    default void onLoad(NextSession nextSession) {}
    default void onCreate(NextSession session) {}
    default void onUpdate(NextSession session) {}
    default void onDelete(Snowflake channel) {}
}
