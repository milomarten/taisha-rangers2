package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import org.springframework.stereotype.Component;

@Component("no")
public class NoCommand extends AbstractSessionPlayerCommand<SessionIdentityParameters> {
    public NoCommand() {
        super("no");

        this.setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doPlayerAction(SessionIdentityParameters params, NextSession session, PlayerResponse pr) {
        pr.no();
        return localizationFactory.createResponse("command.no.response", params.getUsername());
    }
}
