package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.LocalizedStrings;
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

/**
 * A ParameterParser which parses all parameters into one POJO (Plain Old Java Object)
 * This is done by maintaining a list of child ParameterParsers, and an attached setter field.
 * On receipt of an event, the POJO is constructed, then each child ParameterParser is run through, and
 * the associated setter called.
 * The call to `toDiscordSpec()` is done by calling all child ParameterParsers `toDiscordSpec()`, and concatenating
 * all the responses. This means that, for nested PojoParameters, the parameter list sent to Discord is flattened out,
 * although parsed into nested POJOs still. This also means, for sibling PojoParameters, they cannot share
 * any fields from their toDiscordSpec() call.
 * This parser works best for objects with no-arg constructors, or constructors with args that are static
 * over the lifetime of the application. For records, see RecordParameterParserBuilder.
 * @param <PARAM> The object type of the parameter
 */
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

    /**
     * Run some code that will accept the customer usage event and the POJO, and do something.
     * This method is mostly for running code that does not mutate the POJO.
     * withParameterField should be used in all cases where the POJO is being mutated.
     * All methods running from this will run after any added by withParameterField.
     * @param field The function that does something with the event and POJO
     * @return This instance, for chaining
     */
    @Deprecated
    public PojoParameterParser<PARAM> withInteractionField(
            BiConsumer<ChatInputInteractionEvent, PARAM> field) {
        this.fieldsFromInteractions.add(field);
        return this;
    }

    /**
     * Add a setter, which extracts a value from the command usage event and sets it in the POJO
     * Behind the scenes, this uses OneNonParameterParser; as such, this will *not* create a parameter that can
     * be filled in by a user. It is just for extracting context fields, like the channel ID.
     * @param extract The method to extract a value from the command usage event
     * @param setter The method to set that value on the POJO
     * @return This instance, for chaining
     * @param <FIELD> The type of the field being set
     */
    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            Function<ChatInputInteractionEvent, FIELD> extract,
            BiConsumer<PARAM, FIELD> setter) {
        return withParameterField(
                new OneNonParameterParser<>(extract),
                setter
        );
    }

    /**
     * Add a basic Parameter, which extracts a parameter from the command usage event and sets it in the POJO
     * Behind the scenes, this uses OneParameterParser; as such, it will create one parameter that can be
     * filled in by a user.
     * @param name The name of the parameter
     * @param description The description of the parameter
     * @param info The ParameterInfo, which includes type and validation of the parameter
     * @param setter The method to set that value on the POJO
     * @return This instance, for chaining
     * @param <FIELD> The type of the field being set
     */
    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            String name, String description, ParameterInfo<FIELD> info, BiConsumer<PARAM, FIELD> setter) {
        return withParameterField(
                new OneParameterParser<>(name, description, info),
                setter
        );
    }

    /**
     * Add a basic Parameter, which extracts a parameter from the command usage event and sets it in the POJO
     * Behind the scenes, this uses OneParameterParser; as such, it will create one parameter that can be
     * filled in by a user.
     * @param name The name of the parameter
     * @param description The description of the parameter
     * @param info The ParameterInfo, which includes type and validation of the parameter
     * @param setter The method to set that value on the POJO
     * @return This instance, for chaining
     * @param <FIELD> The type of the field being set
     */
    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            LocalizedStrings name, LocalizedStrings description, ParameterInfo<FIELD> info, BiConsumer<PARAM, FIELD> setter) {
        return withParameterField(
                new OneParameterParser<>(name, description, info),
                setter
        );
    }

    /**
     * Adds a setter, which generically calls a child ParameterParser and a setter to set that value on the POJO.
     * This allows, potentially, a POJO which uses another nested POJO within it.
     * @param parser A child parser to use
     * @param setter The setter which sets that value on the POJO
     * @return This instance, for chaining
     * @param <FIELD> The type of the field being set
     */
    public <FIELD> PojoParameterParser<PARAM> withParameterField(
            ParameterParser<FIELD> parser, BiConsumer<PARAM, FIELD> setter) {
        this.fieldsFromParameters.add(
                new PojoSetterParser<>(parser, setter));
        return this;
    }

    /**
     * A utility method which extracts the user ID of the command, and do something with it
     * @param consumer The setter, which accepts the POJO and the User ID of the command
     * @return A BiConsumer which can be used with withInteractionField
     * @param <PARAM> The type of the parameter
     * @deprecated For manipulating the POJO, withInteractionField is deprecated in favor of withParameterField(extract, setter)
     */
    @Deprecated
    public static <PARAM> BiConsumer<ChatInputInteractionEvent, PARAM> userId(BiConsumer<PARAM, Snowflake> consumer) {
        return (event, param) -> {
            consumer.accept(param, event.getUser().getId());
        };
    }

    /**
     * A utility method which extracts the user ID of the command.
     * @return A Function which, when given a command usage event, returns the snowflake of the user
     */
    public static Function<ChatInputInteractionEvent, Snowflake> userId() {
        return event -> event.getUser().getId();
    }

    /**
     * A utility method which extracts the channel ID the command was used in, and do something with it
     * @param consumer The setter, which accepts the POJO and the Channel ID of the command
     * @return A BiConsumer which can be used with withInteractionField
     * @param <PARAM> The type of the parameter
     * @deprecated For manipulating the POJO, withInteractionField is deprecated in favor of withParameterField(extract, setter)
     */
    @Deprecated
    public static <PARAM> BiConsumer<ChatInputInteractionEvent, PARAM> channelId(BiConsumer<PARAM, Snowflake> consumer) {
        return (event, param) -> {
            consumer.accept(param, event.getInteraction().getChannelId());
        };
    }

    /**
     * A utility method which extracts the channel ID of the command.
     * @return A Function which, when given a command usage event, returns the snowflake of the channel it was used in
     */
    public static Function<ChatInputInteractionEvent, Snowflake> channelId() {
        return event -> event.getInteraction().getChannelId();
    }
}
