package com.github.milomarten.taisha_rangers2.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommandPermission {
    MANAGE_CHANNELS(4);

    public final int bitPosition;
}
