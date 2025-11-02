package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PlayerResponse {
    private final Snowflake player;
    private State state = State.NO_RESPONSE;
    private ZonedDateTime afterTime;
    private ZonedDateTime beforeTime;

    public void yes(ZonedDateTime after, ZonedDateTime before) {
        this.state = State.YES;
        this.afterTime = after;
        this.beforeTime = before;
    }

    public void no() {
        this.state = State.NO;
        this.afterTime = null;
        this.beforeTime = null;
    }

    public void maybe(ZonedDateTime at) {
        this.state = State.MAYBE;
        this.afterTime = at;
        this.beforeTime = null;
    }

    public enum State {
        YES,
        NO,
        MAYBE,
        NO_RESPONSE
    }
}
