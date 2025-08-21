package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class RecordParameterParserBuilder<PARAM, CONSTRUCTOR> {
    public abstract ParameterParser<PARAM> build(CONSTRUCTOR constructor);

    @RequiredArgsConstructor
    public static class One<PARAM, A> extends RecordParameterParserBuilder<PARAM, Function<A, PARAM>> {
        private final ParameterParser<A> one;

        public <B> Two<PARAM, A, B> and(ParameterParser<B> other) {
            return new Two<>(one, other);
        }

        public <B> Two<PARAM, A, B> and(String name, String desc, ParameterInfo<B> parameterInfo) {
            return and(new OneParameterParser<>(name, desc, parameterInfo));
        }

        public <B> Two<PARAM, A, B> and(Function<ChatInputInteractionEvent, B> func) {
            return and(new OneNonParameterParser<>(func));
        }

        @Override
        public ParameterParser<PARAM> build(Function<A, PARAM> constructor) {
            return new ParameterParser<>() {
                @Override
                public PARAM parse(ChatInputInteractionEvent event) {
                    return constructor.apply(
                            one.parse(event)
                    );
                }

                @Override
                public List<ApplicationCommandOptionData> toDiscordSpec() {
                    return one.toDiscordSpec();
                }
            };
        }
    }

    @RequiredArgsConstructor
    public static class Two<PARAM, A, B> extends RecordParameterParserBuilder<PARAM, BiFunction<A, B, PARAM>> {
        private final ParameterParser<A> one;
        private final ParameterParser<B> two;

        public N<PARAM> and(ParameterParser<?> other) {
            return new N<>(List.of(one, two, other));
        }

        public N<PARAM> and(String name, String desc, ParameterInfo<?> parameterInfo) {
            return and(new OneParameterParser<>(name, desc, parameterInfo));
        }

        public N<PARAM> and(Function<ChatInputInteractionEvent, ?> func) {
            return and(new OneNonParameterParser<>(func));
        }

        @Override
        public ParameterParser<PARAM> build(BiFunction<A, B, PARAM> constructor) {
            return new ParameterParser<>() {
                @Override
                public PARAM parse(ChatInputInteractionEvent event) {
                    return constructor.apply(
                            one.parse(event),
                            two.parse(event)
                    );
                }

                @Override
                public List<ApplicationCommandOptionData> toDiscordSpec() {
                    return Stream.of(one, two)
                            .map(ParameterParser::toDiscordSpec)
                            .flatMap(List::stream)
                            .toList();
                }
            };
        }
    }

    // 3, 4, 5...?

    @RequiredArgsConstructor
    public static class N<PARAM> extends RecordParameterParserBuilder<PARAM, Function<Object[], PARAM>> {
        private final List<ParameterParser<?>> parsers;

        public N<PARAM> and(ParameterParser<?> other) {
            var copy = new ArrayList<>(parsers);
            copy.add(other);
            return new N<>(copy);
        }

        @Override
        public ParameterParser<PARAM> build(Function<Object[], PARAM> constructor) {
            return new ParameterParser<PARAM>() {
                @Override
                public PARAM parse(ChatInputInteractionEvent event) {
                    var values = parsers.stream()
                            .map(p -> p.parse(event))
                            .toArray();
                    return constructor.apply(values);
                }

                @Override
                public List<ApplicationCommandOptionData> toDiscordSpec() {
                    return parsers.stream()
                            .map(ParameterParser::toDiscordSpec)
                            .flatMap(List::stream)
                            .toList();
                }
            };
        }
    }
}
