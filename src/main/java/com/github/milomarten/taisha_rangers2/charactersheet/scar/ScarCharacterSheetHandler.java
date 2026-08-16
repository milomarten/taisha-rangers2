package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.milomarten.taisha_rangers2.charactersheet.CharacterSheetHandler;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import com.google.api.services.sheets.v4.Sheets;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScarCharacterSheetHandler implements CharacterSheetHandler<ScarCharacterSheet> {
    private final Sheets sheets;
    private final Persister persister;

    private final Map<String, SheetsBackedCharacterSheet> googleSheets = new HashMap<>();
    private final Map<String, InMemoryCharacterSheet> inMemory = new HashMap<>();

    @PostConstruct
    public void load() {
        persister.load("character-sheets", Serial.class)
                .doOnSuccess(s -> {
                    if (s != null) {
                        s.google.forEach(
                                gss -> googleSheets.put(gss.characterId, gss.resolve(sheets))
                        );
                        s.inMemory.forEach(
                                im -> inMemory.put(im.characterId, im.imcs)
                        );
                    }
                })
                .block();
    }

    @Override
    public ScarCharacterSheet getCharacterSheet(String id) {
        if (googleSheets.containsKey(id)) {
            return googleSheets.get(id);
        }
        return inMemory.getOrDefault(id, null);
    }

    @Override
    public void addCharacterSheet(String id, ScarCharacterSheet sheet) {
        switch (sheet) {
            case SheetsBackedCharacterSheet sbcs -> googleSheets.put(id, sbcs);
            case InMemoryCharacterSheet imcs -> inMemory.put(id, imcs);
        }
        persist();
    }

    private void persist() {
        var googles = googleSheets.entrySet()
                .stream()
                .map(e -> new GoogleSheetSerial(e.getKey(), e.getValue().getSpreadsheetId()))
                .toList();
        var im = inMemory.entrySet()
                .stream()
                .map(e -> new InMemorySerial(e.getKey(), e.getValue()))
                .toList();

        this.persister.persist("character-sheets", new Serial(googles, im))
                .subscribe();
    }

    @RequiredArgsConstructor
    public static class Serial {
        private final List<GoogleSheetSerial> google;
        private final List<InMemorySerial> inMemory;
    }

    public record GoogleSheetSerial(String characterId, String spreadsheetId) {
        public SheetsBackedCharacterSheet resolve(Sheets sheets) {
            return new SheetsBackedCharacterSheet(sheets, spreadsheetId());
        }
    }

    public record InMemorySerial(String characterId, InMemoryCharacterSheet imcs) { }
}
