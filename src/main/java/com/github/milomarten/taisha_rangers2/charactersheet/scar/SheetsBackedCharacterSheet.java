package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SheetsBackedCharacterSheet implements ScarCharacterSheet {
    private static final String RANGE_PREFIX = "'Stat Sheet!'";

    private final Sheets sheets;
    private final String spreadsheetId;

    private static String range(String startColumn, int startRow, String endColumn, int endRow) {
        return RANGE_PREFIX + startColumn + startRow + ":" + endColumn + endRow;
    }

    private static Value toValue(List<Object> cols) {
        return new Value((int) cols.get(0), (int) cols.get(1));
    }

    @Override
    @SneakyThrows
    public Map<Attribute, Value> getAttributes() {
        BatchGetValuesResponse status = sheets.spreadsheets().values()
                .batchGet(spreadsheetId)
                .setRanges(List.of(
                        range("L", 12, "M", 14),
                        range("L", 16, "M", 17)
                ))
                .execute();
        var unfolded = status.getValueRanges()
                .stream()
                .flatMap(vr -> vr.getValues().stream())
                .toList();

        var map = new EnumMap<Attribute, Value>(Attribute.class);
        for (int i = 0; i < unfolded.size(); i++) {
            map.put(Attribute.values()[i], toValue(unfolded.get(i)));
        }
        return map;
    }

    @Override
    public Map<Expertise, Value> getExpertises() {
        return Map.of();
    }

    @Override
    public Map<Skill, Value> getSkills() {
        return Map.of();
    }

    @Override
    public Value getAttribute(Attribute attribute) {
        return null;
    }

    @Override
    public Value getExpertise(Expertise expertise) {
        return null;
    }

    @Override
    public Value getSkill(Skill skill) {
        return null;
    }

    @Override
    public void updateAttributes(Map<Attribute, Value> attributes) {

    }

    @Override
    public void updateExpertises(Map<Expertise, Value> expertises) {

    }

    @Override
    public void updateSkills(Map<Skill, Value> skills) {

    }

    @Override
    public HP getHp() {
        return null;
    }

    @Override
    public void setCurrentHp(int qty) {

    }

    @Override
    public void setBonusMaxHp(int qty) {

    }

    @Override
    public void setPreventativeHp(int qty) {

    }

    @Override
    public void setRestorativeHp(int qty) {

    }

    @Override
    public int getEvasion(AttackType attackType) {
        return 0;
    }

    @Override
    public int getIntegrity(AttackType attackType) {
        return 0;
    }

    @Override
    public int getSoulSave() {
        return 0;
    }

    @Override
    public Exhaustion getExhaustion() {
        return null;
    }

    @Override
    public void setExhaustion(int qty) {

    }

    @Override
    public void endOfTurn() {

    }

    @Override
    public void endOfScene() {

    }
}
