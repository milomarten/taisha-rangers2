package com.github.milomarten.taisha_rangers2.config;

import com.github.milomarten.taisha_rangers2.command.CommandHandler;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnBean(GatewayDiscordClient.class)
public class EventManager {
    private final GatewayDiscordClient gateway;

    private final Map<String, CommandSpec<?>> commandHandlers;

    @PostConstruct
    public void init() {
        if (!commandHandlers.isEmpty()) {
            gateway.on(ChatInputInteractionEvent.class, interaction -> {
                var handler = commandHandlers.get(interaction.getCommandName());
                if (handler != null) {
                    return handler.run(interaction)
                            .onErrorResume(ex -> {
                                log.error("Error performing command {}", interaction.getCommandName(), ex);
                                return Mono.empty();
                            });
                } else {
                    return Mono.empty();
                }
            }).subscribe();
        }

        gateway.on(MessageCreateEvent.class, mce -> {
           if (mce.getMessage().getContent().equals("!")) {
               return bulkCreate(902681369405173840L) // Martens & Magic
                       .flatMap(count -> {
                           return mce.getMessage().getChannel()
                                   .flatMap(channel -> channel.createMessage("Created " + count + " commands"));
                       });
           }
           return Mono.empty();
        }).subscribe();
    }

    private Mono<Long> bulkCreate(long guildId) {
        if (commandHandlers.isEmpty()) {
            log.warn("No command handlers found");
            return Mono.just(0L);
        }

        return gateway.getRestClient().getApplicationId()
                .flatMapMany(id -> {
                    var commands = commandHandlers.values().stream()
                                    .map(CommandSpec::toDiscordSpec)
                                    .toList();
                    return gateway.getRestClient().getApplicationService()
                            .bulkOverwriteGuildApplicationCommand(id, guildId, commands);
                })
                .doOnNext(acd -> {
                    log.info("Created command {}", acd.name());
                })
                .onErrorContinue((ex, something) -> {
                    log.error("Unable to create command", ex);
                })
                .count();
    }
}
