package com.github.milomarten.taisha_rangers2.command;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class LocalizedSubCommandSpec extends AbstractCommandSpec {
    private final String id;
    private final Map<String, Path<?>> paths = new HashMap<>();

    protected LocalizationFactory localizationFactory;
    protected Localizer localizer;

    @Setter
    Set<CommandPermission> permissions = Set.of();

    public LocalizedSubCommandSpec(String id) {
        this.id = id;
    }

    protected <T> void addPath(String key, ParameterParser<T> parameterParser, Function<T, CommandResponse> action) {
        this.paths.put(key, new Path<>(parameterParser, action));
    }

    @Autowired
    public void setLocalizationFactory(LocalizationFactory localizationFactory) {
        this.localizationFactory = localizationFactory;
        this.localizer = localizationFactory.withPrefix("command");
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder decorate(ImmutableApplicationCommandRequest.Builder builder) {
        var localName = localizer.localize(id, "name");
        var localDescription = localizer.localize(id, "description");

        builder
                .name(this.id)
                .nameLocalizationsOrNull(localName.getDiscordifiedTranslations())
                .description(localDescription.key())
                .descriptionLocalizationsOrNull(localDescription.getDiscordifiedTranslations());

        var parameterLocalizer = localizer.withPrefix(id).withPrefix("parameter");
        for (var path : paths.entrySet()) {
            var pathName = parameterLocalizer.localize(path.getKey(), "name");
            var pathDescription = parameterLocalizer.localize(path.getKey(), "description");
            builder.addOption(ApplicationCommandOptionData.builder()
                    .name(path.getKey())
                    .nameLocalizationsOrNull(pathName.getDiscordifiedTranslations())
                    .description(pathDescription.key())
                    .descriptionLocalizationsOrNull(pathDescription.getDiscordifiedTranslations())
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                    .addAllOptions(path.getValue().parameterParser.toDiscordSpec(
                            parameterLocalizer.withPrefix(path.getKey()).withPrefix("subparameter")
                    ))
                    .build());
        }
        return builder;
    }

    @Override
    public Mono<?> run(ChatInputInteractionEvent event) {
        var usedOption = event.getOptions()
                .getFirst();
        var path = paths.get(usedOption.getName());
        if (path != null) {
            return path.run(event, usedOption);
        }
        return Mono.empty();
    }

    private record Path<T>(ParameterParser<T> parameterParser, Function<T, CommandResponse> action) {
        public Mono<?> run(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option) {
            var param = parameterParser.parse(event, option);
            return action.apply(param).respond(event);
        }
    }
}
