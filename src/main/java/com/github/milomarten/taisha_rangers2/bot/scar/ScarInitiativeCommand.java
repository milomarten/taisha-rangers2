package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScarInitiativeCommand extends ScarCommand.ScarIdentityParameters {
    private static final Random RANDOM = new Random();

    private int initiative;

    public static ParameterParser<ScarInitiativeCommand> parser() {
        return parser(ScarInitiativeCommand::new)
                .withParameterField("initiative", IntParameter.builder().minValue(0).build(), ScarInitiativeCommand::setInitiative);
    }

    public CommandResponse run(FindPlayerService.PlayerContext playerContext) {
        List<String> rolls = new ArrayList<>();
        int score = 0;
        var rollsLeft = this.initiative + 1;
        while(rollsLeft > 0) {
            rollsLeft--;
            var roll = RANDOM.nextInt(1, 21);
            rolls.add(String.valueOf(roll));
            score += roll;
        }
        var string = playerContext.identity().getName() + " rolled initiative!\n";
        string += "  -> " + "\n\uD83C\uDFB2( " + String.join(", ", rolls) + ") -> **" + score + "**";

        return CommandResponse.reply(string, false);
    }
}
