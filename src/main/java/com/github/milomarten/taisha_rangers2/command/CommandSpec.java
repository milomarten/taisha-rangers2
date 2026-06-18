package com.github.milomarten.taisha_rangers2.command;

import com.github.milomarten.taisha_rangers2.command.parameters.NoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class CommandSpec<PARAM> extends AbstractCommandSpec {
    @Setter @Getter ParameterParser<PARAM> parameterParser = NoParameterParser.create();

    @Override
    public Mono<?> run(ChatInputInteractionEvent event) {
        return Mono.fromCallable(() -> parameterParser.parse(event))
                .flatMap(params -> Mono.fromCallable(() -> doAction(params)))
                .flatMap(cr -> cr.respond(event))
                .then()
                .onErrorResume(ex -> handleException(event, ex))
                .onErrorResume(ex -> {
                    log.error("Error sending error message. Doing nothing...", ex);
                    return Mono.empty();
                });
    }

    protected abstract CommandResponse doAction(PARAM params);

    protected Mono<Void> handleException(ChatInputInteractionEvent event, Throwable ex) {
        log.error("Error running command. Telling user...", ex);
        return CommandResponse.reply(ex.getMessage(), true)
                .respond(event)
                .then();
    }
}
