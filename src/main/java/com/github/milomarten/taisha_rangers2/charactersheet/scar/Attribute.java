package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Attribute {
    BODY(AttributeType.PRIMARY),
    MIND(AttributeType.PRIMARY),
    SOUL(AttributeType.PRIMARY),
    VIGOR(AttributeType.SECONDARY),
    WILLPOWER(AttributeType.SECONDARY);

    private final AttributeType attributeType;

    public enum AttributeType {
        PRIMARY,
        SECONDARY
    }
}
