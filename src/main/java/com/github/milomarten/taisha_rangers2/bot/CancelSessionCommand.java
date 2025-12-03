package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("cancel")
public class CancelSessionCommand extends CommandSpec<SessionIdentityParameters> {
    private final NextSessionManager manager;

    public CancelSessionCommand(NextSessionManager manager) {
        super("cancel", "Cancel the upcoming session");
        this.manager = manager;

        setParameterParser(SessionIdentityParameters.parser());
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(SessionIdentityParameters params) {
        var worked = manager.cancelSession(params);
        if (worked) {
            return CommandResponse.reply("Session was canceled. Maybe next time!", false);
        } else {
            return CommandResponse.reply("No session???", true);
        }
    }
}
