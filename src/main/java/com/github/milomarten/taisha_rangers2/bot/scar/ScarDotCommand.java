package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScarDotCommand extends ScarCommand.ScarIdentityParameters {
    private static final Random RANDOM = new Random();

    private int numDice;
    private int difficulty;
    private int explodeAt;
    private String comment;

    public static ParameterParser<ScarDotCommand> parser() {
        return parser(ScarDotCommand::new)
                .withParameterField("numdice", IntParameter.builder().minValue(0).maxValue(30).build(), ScarDotCommand::setNumDice)
                .withParameterField("difficulty", IntParameter.builder().defaultValue(7).minValue(5).maxValue(9).build(), ScarDotCommand::setDifficulty)
                .withParameterField("explode", IntParameter.builder().defaultValue(10).minValue(8).maxValue(11).build(), ScarDotCommand::setExplodeAt)
                .withParameterField("comment", StringParameter.DEFAULT_EMPTY_STRING, ScarDotCommand::setComment);
    }

    public CommandResponse run(String name) {
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
        var string = name + " did a dot roll!";
        if (!this.comment.isEmpty()) {
            string = string + " " + comment;
        }
        string += this.numDice + "•d10 -> " + "\n\uD83C\uDFB2( " + String.join(", ", rolls) + ") -> **" + score + "**";

        return CommandResponse.reply(string, false);
    }
}
