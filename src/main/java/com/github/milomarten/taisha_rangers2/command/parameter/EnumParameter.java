package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.Optional;

public class EnumParameter<E extends Enum<E>> implements ParameterInfo<E> {
    private final E[] universe;
    private final E defaultValue;

    public EnumParameter(Class<E> enumClass) {
        this(enumClass, null);
    }

    public EnumParameter(Class<E> enumClass, E defaultValue) {
        this.universe = enumClass.getEnumConstants();
        this.defaultValue = defaultValue;
    }

    @Override
    public E convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .map(s -> ArrayUtils.get(universe, s.intValue()))
                .or(() -> Optional.ofNullable(defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
        var choices = Arrays.stream(universe)
                .map(e -> (ApplicationCommandOptionChoiceData) ApplicationCommandOptionChoiceData.builder()
                        .name(e.name())
                        .value(e.ordinal())
                        .build())
                .toList();
        return builder
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .required(defaultValue == null)
                .addAllChoices(choices);
    }
}
