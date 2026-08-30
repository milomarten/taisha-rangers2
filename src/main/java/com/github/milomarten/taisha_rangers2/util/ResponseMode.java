package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Objects;

public enum ResponseMode {
    PUBLIC {
        @Override
        public CommandResponse respond(String message, FindPlayerService.PlayerContext context) {
            return CommandResponse.reply(message, false);
        }
    },
    EPHEMERAL {
        @Override
        public CommandResponse respond(String message, FindPlayerService.PlayerContext context) {
            return CommandResponse.reply(message, true);
        }
    },
    DIRECT_MESSAGE {
        @Override
        public CommandResponse respond(String message, FindPlayerService.PlayerContext context) {
            return event -> event.getUser()
                    .getPrivateChannel()
                    .flatMap(pc -> pc.createMessage(message))
                    .map(msg -> CommandResponse.reply("DMed you!", true))
                    .defaultIfEmpty(CommandResponse.reply("Unable to DM you!\n" + message, true))
                    .flatMap(rr -> rr.respond(event))
                    .then();
        }
    },
    EPHEMERAL_AND_STORYTELLER {
        @Override
        public CommandResponse respond(String message, FindPlayerService.PlayerContext context) {
            if (context.party() == null) {
                return CommandResponse.reply("I don't know your storyteller!\n" + message, true);
            } else if (Objects.equals(context.user(), context.party().getDm())) {
                return CommandResponse.reply("You are the storyteller!\n" + message, true);
            } else {
                return event -> {
                    var storytellerDm = event.getClient()
                            .getUserById(context.party().getDm())
                            .flatMap(User::getPrivateChannel)
                            .flatMap(pc -> pc.createMessage(message))
                            .onErrorResume(ex -> Mono.empty());

                    var playerEphemeral = CommandResponse.reply(message, true)
                            .respond(event);

                    return playerEphemeral.then(storytellerDm).then();
                };
            }
        }
    };

    public abstract CommandResponse respond(String message, FindPlayerService.PlayerContext context);
}
