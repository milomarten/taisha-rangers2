package com.github.milomarten.taisha_rangers2.command.autocomplete;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatchTest {
    @Test
    public void fuzzyMatchTest() {
        String input = "pe";
        List<String> possibilities = List.of("private", "pods", "peanut");

        var match = FuzzyMatch.lazyFuzzyMatch(input, possibilities);
        assertEquals(List.of("peanut", "private"), match);
    }
}