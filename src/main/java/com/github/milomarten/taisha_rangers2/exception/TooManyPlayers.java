package com.github.milomarten.taisha_rangers2.exception;

public class TooManyPlayers extends RuntimeException {
    public TooManyPlayers(int max) {
        super("Only expected " + max + " players");
    }
}
