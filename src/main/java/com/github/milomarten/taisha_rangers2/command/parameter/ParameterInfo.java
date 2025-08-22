package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;

import java.util.function.Function;

/**
 * Encapsulates a way to extract a field from a command usage.
 * The purpose of this interface is twofold:
 * 1. Extract the field from the command usage in the correct time
 * 2. Maintain any validations, either client-side or server-side.
 * <br>
 * As a common pattern, all built-in ParameterInfo fields have an option for a defaultValue.
 * If the defaultValue is null, the parameter is considered REQUIRED. If the defaultValue
 * is anything else, the parameter is considered OPTIONAL, and will use that defaultValue when
 * the command doesn't have it. As such, the convert method will never return null.
 * @param <T> The type extracted from the command usage.
 */
public interface ParameterInfo<T> {
    /**
     * Pull a field from the command usage and convert it to the correct type.
     * @param event The command usage to pull from
     * @param field The name of the field to pull
     * @return The value used in the command, of the correct type.
     */
    T convert(ChatInputInteractionEvent event, String field);

    /**
     * Decorate the parameter spec with additional info
     * @param builder The builder to build on top of
     * @return A builder with features specific to this ParameterInfo added
     */
    ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder);

    /**
     * Add a mapping step to a basic ParameterInfo which converts a type into another type.
     * The field is decorated in the exact same way as the source.
     * @param func The mapping function
     * @return A ParameterInfo that represents this ParameterInfo with an additional step after initial extraction.
     * @param <U> The type being converted into.
     */
    default <U> ParameterInfo<U> map(Function<T, U> func) {
        var self = this;
        return new ParameterInfo<U>() {
            @Override
            public U convert(ChatInputInteractionEvent event, String field) {
                return func.apply(self.convert(event, field));
            }

            @Override
            public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder) {
                return self.decorate(builder);
            }
        };
    }
}
