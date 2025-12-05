package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.exception.NotInParty;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;

public abstract class AbstractSessionPlayerCommand<PARAM extends SessionIdentityParameters> extends AbstractSessionCommand<PARAM> {
    public AbstractSessionPlayerCommand(String id) {
        super(id);
    }

    @Override
    protected CommandResponse doAction(PARAM params) {
        try {
            return getManager().playerDoAndReturn(params.getChannelId(), params.getUserId(), (session, pr) -> {
                return doPlayerAction(params, session, pr);
            }).orElseGet(() -> {
                return localizationFactory.createResponse("errors.session.no-match").ephemeral(true);
            });
        } catch (NotInParty e) {
            return localizationFactory.createResponse("errors.party.not-present").ephemeral(true);
        }
    }

    @Override
    protected CommandResponse doAction(PARAM params, NextSession session) {
        throw new UnsupportedOperationException();
    }

    protected abstract CommandResponse doPlayerAction(PARAM params, NextSession session, PlayerResponse pr);
}
