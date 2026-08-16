package com.github.milomarten.taisha_rangers2.charactersheet.scar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Skill {
    ATHLETICS(SkillType.NATURAL),
    BRAWL(SkillType.NATURAL),
    EMIT(SkillType.NATURAL),
    INITIATIVE(SkillType.NATURAL),
    INSIGHT(SkillType.NATURAL),
    INTEGRITY(SkillType.NATURAL),
    PERCEPTION(SkillType.NATURAL),
    PERSUASION(SkillType.NATURAL),
    PRESENCE(SkillType.NATURAL),

    ACROBATICS(SkillType.TRAINED),
    CLASH(SkillType.TRAINED),
    DECEPTION(SkillType.TRAINED),
    EVASION(SkillType.TRAINED),
    MARKSMAN(SkillType.TRAINED),
    NAVIGATION(SkillType.TRAINED),
    PERFORM(SkillType.TRAINED),
    SUBTERFUGE(SkillType.TRAINED),
    SURVIVAL(SkillType.TRAINED),

    ACADEMICS(SkillType.KNOWLEDGE),
    APPRAISAL(SkillType.KNOWLEDGE),
    CRAFT(SkillType.KNOWLEDGE),
    ETIQUETTE(SkillType.KNOWLEDGE),
    INVESTIGATION(SkillType.KNOWLEDGE),
    LORE(SkillType.KNOWLEDGE),
    MEDICINE(SkillType.KNOWLEDGE),
    SCIENCE(SkillType.KNOWLEDGE),
    TECHNOLOGY(SkillType.KNOWLEDGE)
    ;

    private final SkillType skillType;

    public enum SkillType {
        NATURAL,
        TRAINED,
        KNOWLEDGE
    }
}
