package com.github.milomarten.taisha_rangers2.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Rarity {
    Incredibly_common("an", "incredibly common"),
    Very_common("a", "very common"),
    Fairly_common("a", "fairly common"),
    Fairly_uncommon("a", "fairly uncommon"),
    Quite_uncommon("a", "quite uncommon"),
    Very_uncommon("a", "very uncommon"),
    Fairly_rare("a", "fairly rare"),
    Quite_rare("a", "quite rare"),
    Very_rare("a", "very rare"),
    Incredibly_rare("an", "incredibly rare");

    private final String article;
    private final String text;
}
