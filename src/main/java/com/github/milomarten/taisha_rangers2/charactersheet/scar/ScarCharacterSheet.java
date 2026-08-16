package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import java.util.Map;

public sealed interface ScarCharacterSheet permits InMemoryCharacterSheet, SheetsBackedCharacterSheet {
    Map<Attribute, Value> getAttributes();
    Map<Expertise, Value> getExpertises();
    Map<Skill, Value> getSkills();

    Value getAttribute(Attribute attribute);
    Value getExpertise(Expertise expertise);
    Value getSkill(Skill skill);

    void updateAttributes(Map<Attribute, Value> attributes);
    void updateExpertises(Map<Expertise, Value> expertises);
    void updateSkills(Map<Skill, Value> skills);

    default int getAttributeScale() {
        var average = getAttributes()
                .values()
                .stream()
                .mapToInt(Value::total)
                .average()
                .orElse(0);
        return (int) average; // attribute scale explicitly truncates, not rounding to the nearest whole
    }

    default int getPrimaryScale() {
        var average = getAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getAttributeType() == Attribute.AttributeType.PRIMARY)
                .mapToInt(e -> e.getValue().total())
                .average()
                .orElse(0);
        return (int) Math.round(average);
    }

    default int getSecondaryScale(){
        var average = getAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getAttributeType() == Attribute.AttributeType.SECONDARY)
                .mapToInt(e -> e.getValue().total())
                .average()
                .orElse(0);
        return (int) Math.round(average);
    }

    HP getHp();
    void setCurrentHp(int qty);
    void setBonusMaxHp(int qty);
    void setPreventativeHp(int qty);
    void setRestorativeHp(int qty);

    int getEvasion(AttackType attackType);
    int getIntegrity(AttackType attackType);
    int getSoulSave();

    Exhaustion getExhaustion();
    void setExhaustion(int qty);

    void endOfTurn();
    void endOfScene();
}
