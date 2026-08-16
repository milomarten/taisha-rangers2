package com.github.milomarten.taisha_rangers2.charactersheet.scar;

public record Exhaustion(int amount, int penalty) {
    public Exhaustion(int amount) {
        this(amount, computePenalty(amount));
    }

    private static int computePenalty(int amount) {
        if (amount >= 4 && amount < 8) {
            return -1;
        } else if (amount < 12) {
            return -3;
        } else if (amount < 16) {
            return  -5;
        } else if (amount < 20) {
            return -7;
        } else {
            return -9;
        }
    }
}
