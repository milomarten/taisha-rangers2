package com.github.milomarten.taisha_rangers2.exception;

public class NotDM extends RuntimeException {
    public NotDM() {
        super("You're not the DM!");
    }
}
