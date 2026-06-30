package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.parameter.EnumParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.core.object.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScarDotCommand extends ScarCommand.ScarIdentityParameters {
    private static final Random RANDOM = new Random();

    private int numDice;
    private int difficulty;
    private int explodeAt;
    private String comment;
    private ResponseMode mode;

    public static ParameterParser<ScarDotCommand> parser() {
        return parser(ScarDotCommand::new)
                .withParameterField("numdice", IntParameter.builder().minValue(0).maxValue(30).build(), ScarDotCommand::setNumDice)
                .withParameterField("difficulty", IntParameter.builder().defaultValue(7).minValue(5).maxValue(9).build(), ScarDotCommand::setDifficulty)
                .withParameterField("explode", IntParameter.builder().defaultValue(10).minValue(8).maxValue(11).build(), ScarDotCommand::setExplodeAt)
                .withParameterField("comment", StringParameter.DEFAULT_EMPTY_STRING, ScarDotCommand::setComment)
                .withParameterField("mode", new EnumParameter<>(ResponseMode.class, ResponseMode.PUBLIC), ScarDotCommand::setMode)
                ;
    }

    public CommandResponse run(FindPlayerService.PlayerContext playerContext) {
        List<String> rolls = new ArrayList<>();
        int score = 0;
        var rollsLeft = this.numDice;
        while(rollsLeft > 0) {
            rollsLeft--;
            var roll = RANDOM.nextInt(1, 11);
            if (roll == 1) {
                rolls.add(roll + "❌");
                score -= 1;
            } else if (roll >= this.explodeAt) {
                rolls.add(roll + "\uD83D\uDCA5");
                score += 2;
                rollsLeft++;
            } else if (roll >= this.difficulty) {
                rolls.add(roll + "✅");
                score += 1;
            } else {
                rolls.add(String.valueOf(roll));
            }
        }
        var string = playerContext.identity().getName() + " did a dot roll!\n";
        if (!this.comment.isEmpty()) {
            string = string + comment + "\n";
        }
        string += this.numDice + "•d10 -> " + "\n\uD83C\uDFB2( " + String.join(", ", rolls) + ") -> **" + score + "**";

        return mode.respond(string, playerContext);
    }

    private enum ResponseMode {
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
}
