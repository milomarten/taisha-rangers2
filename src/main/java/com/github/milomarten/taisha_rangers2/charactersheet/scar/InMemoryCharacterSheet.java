package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import lombok.Builder;
import lombok.Singular;

import java.util.EnumMap;
import java.util.Map;

@Builder
public final class InMemoryCharacterSheet implements ScarCharacterSheet {
    private static final Value DEFAULT_ONE = new Value(1, 0);
    private static final Value DEFAULT_ZERO = new Value(0, 0);

    @Singular private final Map<Attribute, Value> attributes = new EnumMap<>(Attribute.class);
    @Singular private final Map<Expertise, Value> expertises = new EnumMap<>(Expertise.class);
    @Singular private final Map<Skill, Value> skills = new EnumMap<>(Skill.class);

    private int currentHp;
    private int bonusMaxHp;
    private int prevHp;
    private int resHp;

    private Exhaustion exhaustion;

    @Override
    public Map<Attribute, Value> getAttributes() {
        return new EnumMap<>(attributes);
    }

    @Override
    public Map<Expertise, Value> getExpertises() {
        return new EnumMap<>(expertises);
    }

    @Override
    public Map<Skill, Value> getSkills() {
        return new EnumMap<>(skills);
    }

    @Override
    public Value getAttribute(Attribute attribute) {
        return attributes.getOrDefault(attribute, DEFAULT_ONE);
    }

    @Override
    public Value getExpertise(Expertise expertise) {
        return expertises.getOrDefault(expertise, DEFAULT_ZERO);
    }

    @Override
    public Value getSkill(Skill skill) {
        return skills.getOrDefault(skill, DEFAULT_ZERO);
    }

    @Override
    public void updateAttributes(Map<Attribute, Value> attributes) {
        this.attributes.putAll(attributes);
    }

    @Override
    public void updateExpertises(Map<Expertise, Value> expertises) {
        this.expertises.putAll(expertises);
    }

    @Override
    public void updateSkills(Map<Skill, Value> skills) {
        this.skills.putAll(skills);
    }

    @Override
    public HP getHp() {
        var maxHp = attributes.values().stream()
                .mapToInt(Value::total)
                .sum() + this.bonusMaxHp;
        return new HP(this.currentHp + this.resHp + this.prevHp, maxHp);
    }

    @Override
    public void setCurrentHp(int qty) {
        this.currentHp = qty;
    }

    public void setCurrentHpToMax() {
        this.currentHp = getHp().maxHp();
    }

    @Override
    public void setBonusMaxHp(int qty) {
        this.bonusMaxHp = qty;
    }

    @Override
    public void setPreventativeHp(int qty) {
        this.prevHp = Math.max(this.prevHp, qty);
    }

    @Override
    public void setRestorativeHp(int qty) {
        int missingHp = getHp().maxHp() - currentHp;
        if (missingHp > 0) {
            this.resHp = Math.min(qty, missingHp);
        }
    }

    @Override
    public int getEvasion(AttackType attackType) {
        return getDefensiveScore(attackType, Skill.EVASION);
    }

    @Override
    public int getIntegrity(AttackType attackType) {
        return getDefensiveScore(attackType, Skill.INTEGRITY);
    }

    @Override
    public int getSoulSave() {
        return getDefensiveScore(AttackType.STATUS, Skill.INTEGRITY);
    }

    private int getDefensiveScore(AttackType attackType, Skill skill) {
        var attribute = switch (attackType) {
            case PHYSICAL -> getAttribute(Attribute.BODY);
            case ETHEREAL -> getAttribute(Attribute.MIND);
            case STATUS -> getAttribute(Attribute.SOUL);
        };
        var expertise = highestExpertise();
        var skillV = getSkill(skill);

        return divideAndRound(attribute.total() + expertise + skillV.total(), 2);
    }

    @Override
    public Exhaustion getExhaustion() {
        return this.exhaustion;
    }

    @Override
    public void setExhaustion(int qty) {
        this.exhaustion = new Exhaustion(qty);
    }

    private int highestExpertise() {
        return expertises.values()
                .stream()
                .mapToInt(Value::total)
                .max()
                .orElse(0);
    }

    private static int divideAndRound(int numerator, int denominator) {
        var converted = ((double) numerator) / ((double) denominator);
        return (int) (Math.round(converted));
    }

    @Override
    public void endOfTurn() {
        var hp = getHp();
        if (hp.currentHp() < 0 && hp.state() != HP.State.DEAD) {
            currentHp--;
        }
    }

    @Override
    public void endOfScene() {
        this.resHp = 0;
        var hp = getHp();
        if (hp.currentHp() <= 0) {
            this.prevHp = 1;
        }
    }
}
