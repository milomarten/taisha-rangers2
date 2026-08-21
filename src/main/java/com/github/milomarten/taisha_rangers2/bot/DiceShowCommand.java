package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.dice.DiceService;
import org.springframework.stereotype.Component;

@Component("dice-show")
public class DiceShowCommand extends LocalizedCommandSpec<SessionIdentityParameters> {
    private final DiceService diceService;
    public DiceShowCommand(DiceService diceService) {
        super("dice-show");
        this.diceService = diceService;
        setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doAction(SessionIdentityParameters params) {
        var results = this.diceService.getDice(
                params.getUserId().asString()
        );

        return localizationFactory.createResponse("command.dice-show.response",
                results.asString(false)
        );
    }
}
