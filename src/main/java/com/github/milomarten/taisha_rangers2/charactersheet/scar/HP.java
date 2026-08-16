package com.github.milomarten.taisha_rangers2.charactersheet.scar;

public record HP(int currentHp, int maxHp) {
    public State state() {
        if (currentHp >= maxHp) {
            return State.FULLY_HEALED;
        } else if (currentHp > 0) {
            return State.ALIVE;
        } else if (currentHp > -maxHp / 2) {
            return State.WINDED;
        } else if (currentHp > -maxHp) {
            return State.UNCONSCIOUS;
        } else {
            return State.DEAD;
        }
    }

    public enum State {
        FULLY_HEALED,
        ALIVE,
        WINDED,
        UNCONSCIOUS,
        DEAD
    }
}
