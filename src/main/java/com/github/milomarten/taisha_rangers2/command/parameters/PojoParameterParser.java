package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.possible.Possible;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PojoParameterParser<PARAM> implements ParameterParser<PARAM> {
    private final Supplier<PARAM> constructor;
    private final List<PojoSetterParser<PARAM, ?>> fieldsFromParameters = new ArrayList<>();
    private final List<BiConsumer<ChatInputInteractionEvent, PARAM>> fieldsFromInteractions = new ArrayList<>();

    private static final Comparator<Possible<Boolean>> REQUIRED_FIRST = Comparator.comparing(
            poss -> poss.toOptional().orElse(false),
            Comparator.reverseOrder()
    );

    @Override
    public PARAM parse(ChatInputInteractionEvent event) {
        var instance = constructor.get();
        fieldsFromParameters.forEach(field -> field.set(event, instance));
        fieldsFromInteractions.forEach(field -> field.accept(event, instance));
        return instance;
    }

    @Override
    public List<ApplicationCommandOptionData> toDiscordSpec() {
        return fieldsFromParameters.stream()
                .map(PojoSetterParser::toDiscordSpec)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ApplicationCommandOptionData::required, REQUIRED_FIRST))
                .toList();
    }

    @Deprecated
    public PojoParameterParser<PARAM> withInteractionField(
            BiConsumer<ChatInputInteractionEvent, PARAM> field) {
        this.fieldsFromInteractions.add(field);
        return this;
    }

    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            Function<ChatInputInteractionEvent, FIELD> extract,
            BiConsumer<PARAM, FIELD> setter) {
        return withParameterField(
                new OneNonParameterParser<>(extract),
                setter
        );
    }

    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            String name, String description, ParameterInfo<FIELD> info, BiConsumer<PARAM, FIELD> setter) {
        return withParameterField(
                new OneParameterParser<>(name, description, info),
                setter
        );
    }

    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            ParameterParser<FIELD> parser, BiConsumer<PARAM, FIELD> setter) {
        this.fieldsFromParameters.add(
                new PojoSetterParser<>(parser, setter));
        return this;
    }

    public static <PARAM> BiConsumer<ChatInputInteractionEvent, PARAM> user(BiConsumer<PARAM, User> consumer) {
        return (event, param) -> {
            consumer.accept(param, event.getUser());
        };
    }

    public static <PARAM> BiConsumer<ChatInputInteractionEvent, PARAM> userId(BiConsumer<PARAM, Snowflake> consumer) {
        return (event, param) -> {
            consumer.accept(param, event.getUser().getId());
        };
    }

    public static <PARAM> BiConsumer<ChatInputInteractionEvent, PARAM> channelId(BiConsumer<PARAM, Snowflake> consumer) {
        return (event, param) -> {
            consumer.accept(param, event.getInteraction().getChannelId());
        };
    }
}
