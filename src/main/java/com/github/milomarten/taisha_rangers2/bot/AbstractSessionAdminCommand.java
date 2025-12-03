package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractSessionAdminCommand<PARAM extends SessionIdentityParameters> extends AbstractSessionCommand<PARAM> {
    public AbstractSessionAdminCommand(String id) {
        super(id);
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    protected final CommandResponse doAction(PARAM params, NextSession session) {
        if (Objects.equals(session.getGm(), params.getUserId())) {
            return doProtectedAction(params, session);
        } else {
            return localizationFactory.createResponse("errors.session.no-access")
                    .ephemeral(true);
        }
    }

    protected abstract CommandResponse doProtectedAction(PARAM params, NextSession session);
}
