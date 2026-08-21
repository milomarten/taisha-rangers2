package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.dice.DiceService;
import org.springframework.stereotype.Component;

@Component("dice-jail")
public class DiceJailCommand extends LocalizedCommandSpec<SessionIdentityParameters> {
    private final DiceService diceService;
    public DiceJailCommand(DiceService diceService) {
        super("dice-jail");
        this.diceService = diceService;
        setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doAction(SessionIdentityParameters params) {
        var results = this.diceService.jail(
                params.getUserId().asString()
        );

        return localizationFactory.createResponse("command.dice-jail.response",
                results.oldOne().asString(false), results.newOne().asString(true)
        );
    }
}
