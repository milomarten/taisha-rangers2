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

/**
 * A ParameterParser which parses all parameters into a record
 * This inverts the logic of the PojoParameterParser; whereas it creates the POJO, then reads through
 * each field and sets them, this one reads through each fields, then creates the record. This is useful
 * if you prefer to use records, or classes with final fields.
 * <br>
 * Since the constructor can have any number of arguments, this builder is responsible for maintaining type
 * safety. Every time a builder is `and()`, it should ascend to the next level of types. At the end, when
 * `build()` is called, it will be a type-safe lambda call.
 * <br>
 * At the highest level, calling `and()` will revert to wrapping all fields into an Object array, eschewing
 * type safety for simply supporting any number of arguments. As of writing, only 1 and 2 params are supported, since
 * Java natively comes with Function and BiFunction.
 * @param <PARAM> The parameter returned from the RecordParameterParser
 * @param <CONSTRUCTOR> The type which encapsulates a lambda which takes all parameters and converts them into one object.
 */
public abstract class RecordParameterParserBuilder<PARAM, CONSTRUCTOR> {
    /**
     * Create the first parameter
     * @param one THe ParameterParser to use for the first field.
     * @return An instance of a RecordParameterParserBuilder, to keep chaining.
     * @param <PARAM> The final parameter type to be constructed
     * @param <A> The type of the first field
     */
    public static <PARAM, A> One<PARAM, A> create(ParameterParser<A> one) {
        return new One<>(one);
    }

    /**
     * Construct the final ParameterParser, using the provided constructor to combine all the fields into one.
     * @param constructor The constructor to use
     * @return The ParameterParser which performs all these steps.
     */
    public abstract ParameterParser<PARAM> build(CONSTRUCTOR constructor);

    @RequiredArgsConstructor
    public static class One<PARAM, A> extends RecordParameterParserBuilder<PARAM, Function<A, PARAM>> {
        private final ParameterParser<A> one;

        /**
         * Create the second parameter
         * @param other The next parameter to add
         * @return An instance of a RecordParameterParserBuilder, to keep chaining.
         * @param <B> The type of the second parameter
         */
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

        /**
         * Create the third parameter
         * At this point, the constructor must be able to handle an array of objects, and require casting.
         * @param other The next parameter to add
         * @return An instance of a RecordParameterParserBuilder, to keep chaining.
         */
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

        /**
         * Add another parameter
         * Calling this method will generate a completely new instance everytime, doing a shallow copy of the
         * parsers encountered so far.
         * @param other The next parameter to add
         * @return An instance of a RecordParameterParserBuilder, to keep chaining.
         */
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
