package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * A ParameterInfo which can extract en Enum from the command usage.
 * Behind the scenes, this is a wrapper around a parameter of type Integer with a list of choices.
 * Discord will enforce that only those Integers are supported; however, if an invalid Integer
 * is somehow provided, the defaultValue will be returned, as a safeguard.
 * A namer parameter is provided, which is used to convert the enum into a String to display in the list of options
 * to the user. By default, it will use the name() function attached to the enum.
 * @param <E> The enum type.
 */
public class EnumParameter<E extends Enum<E>> implements ParameterInfo<E> {
    private final E[] universe;
    private final E defaultValue;
    private Function<E, String> namer = E::name;

    /**
     * Create a required Enum parameter
     * @param enumClass The class of the enum
     */
    public EnumParameter(Class<E> enumClass) {
        this(enumClass, null);
    }

    /**
     * Create a (potentially) optional Enum parameter.
     * If the defaultValue is null, the parameter is considered required; otherwise, it's optional.
     * @param enumClass The class of the enum
     * @param defaultValue The default value to return
     */
    public EnumParameter(Class<E> enumClass, E defaultValue) {
        this.universe = enumClass.getEnumConstants();
        this.defaultValue = defaultValue;
    }

    /**
     * Specify the way to convert the enum into a human-friendly string.
     * @param namer The naming function to use
     * @return This instance, for chaining.
     */
    public EnumParameter<E> namer(Function<E, String> namer) {
        this.namer = namer;
        return this;
    }

    @Override
    public E convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsLong(field)
                .map(s -> ArrayUtils.get(universe, s.intValue()))
                .or(() -> Optional.ofNullable(defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        var choices = Arrays.stream(universe)
                .map(e -> {
                    var localized = localizer.localize(namer.apply(e));
                    return (ApplicationCommandOptionChoiceData) ApplicationCommandOptionChoiceData.builder()
                            .name(localized.key())
                            .nameLocalizationsOrNull(localized.getDiscordifiedTranslations())
                            .value(e.ordinal())
                            .build();
                })
                .toList();
        return builder
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .required(defaultValue == null)
                .addAllChoices(choices);
    }
}
