package com.github.milomarten.taisha_rangers2.command;

import com.github.milomarten.taisha_rangers2.command.parameters.NoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class CommandSpec<PARAM> implements CommandHandler {
    private final String name;
    private final String description;
    @Setter
    ParameterParser<PARAM> parameterParser = NoParameterParser.create();
    @Setter Set<CommandPermission> permissions = Set.of();

    public ApplicationCommandRequest toDiscordSpec() {
        return ApplicationCommandRequest.builder()
                .name(name)
                .description(description)
                .addAllOptions(parameterParser.toDiscordSpec())
                .defaultMemberPermissions(computeMemberPermissions())
                .build();
    }

    @Override
    public Mono<?> run(ChatInputInteractionEvent event) {
        return Mono.fromCallable(() -> parameterParser.parse(event))
                .flatMap(params -> Mono.fromCallable(() -> doAction(params)))
                .flatMap(cr -> cr.respond(event))
                .then()
                .onErrorResume(ex -> {
                    log.error("Error running command. Telling user...", ex);
                    return CommandResponse.reply(ex.getMessage(), true)
                            .respond(event)
                            .then();
                })
                .onErrorResume(ex -> {
                    log.error("Error sending error message. Doing nothing...", ex);
                    return Mono.empty();
                });
    }

    public abstract CommandResponse doAction(PARAM params);

    private String computeMemberPermissions() {
        BigInteger perms = BigInteger.ZERO;
        for (var perm : permissions) {
            BigInteger permAsBigInt = BigInteger.ONE.shiftLeft(perm.bitPosition);
            perms = perms.or(permAsBigInt);
        }
        return perms.toString();
    }
}
