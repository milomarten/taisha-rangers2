package com.github.milomarten.taisha_rangers2.charactersheet.scar;

public record Value(int amount, int bonus) {
    public int total() {
        return amount + bonus;
    }
}
