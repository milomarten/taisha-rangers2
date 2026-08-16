package com.github.milomarten.taisha_rangers2.charactersheet;

import java.util.List;

public interface CharacterSheetHandler<T> {
    T getCharacterSheet(String id);
    void addCharacterSheet(String id, T sheet);
}
