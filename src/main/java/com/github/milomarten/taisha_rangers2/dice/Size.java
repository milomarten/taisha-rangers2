package com.github.milomarten.taisha_rangers2.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Size {
    Bean("bean"),
    Blueberry("blueberry"),
    Fig("fig"),
    Fist("fist"),
    Grape("grape"),
    Hazelnut("hazelnut"),
    Kumquat("kumquat"),
    Lemon("lemon"),
    Lentil("lentil"),
    Lime("lime"),
    Pea("pea"),
    Peanut("peanut"),
    Strawberry("strawberry"),
    Walnut("walnut");

    private final String name;
}
