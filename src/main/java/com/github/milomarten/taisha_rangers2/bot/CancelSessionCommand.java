package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("cancel")
public class CancelSessionCommand extends LocalizedCommandSpec<SessionIdentityParameters> {
    private final NextSessionManager manager;

    public CancelSessionCommand(NextSessionManager manager) {
        super("cancel");
        this.manager = manager;

        setParameterParser(SessionIdentityParameters.parser());
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(SessionIdentityParameters params) {
        var session = manager.getNextSession(params.getChannelId());
        if (session.isPresent()) {
            manager.cancelSession(params);
            return localizationFactory.createResponse("command.cancel.response", FormatUtils.pingRole(session.get().getPing()))
                    .ephemeral(false);
        } else {
            return localizationFactory.createResponse("errors.session.no-match")
                    .ephemeral(true);
        }
    }
}
