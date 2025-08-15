package com.github.milomarten.taisha_rangers2.state;

public enum SessionState {
    /**
     * Session has not yet been announced
     */
    PENDING_ANNOUNCEMENT,
    /**
     * A proposed session time was announced, and waiting for all players to respond
     */
    PENDING_PLAYER_RESPONSE,
    /**
     * All players have responded, and waiting for a final time to be set
     */
    PENDING_START,
    /**
     * Final session time was scheduled, but session has not yet started
     */
    SCHEDULED,
    /**
     * Session has started, but has not ended
     */
    IN_PROGRESS,
    /**
     * This session is complete
     */
    ENDED
}
