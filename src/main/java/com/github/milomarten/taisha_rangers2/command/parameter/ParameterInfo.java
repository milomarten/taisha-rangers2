package com.github.milomarten.taisha_rangers2.command.parameter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;

import java.util.function.Function;

public interface ParameterInfo<T> {
    T convert(ChatInputInteractionEvent event, String field);
    ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder);

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
