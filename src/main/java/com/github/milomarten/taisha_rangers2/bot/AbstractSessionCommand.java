package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSessionCommand<PARAM extends SessionIdentityParameters> extends LocalizedCommandSpec<PARAM> {
    @Getter private NextSessionManager manager;

    public AbstractSessionCommand(String id) {
        super(id);
    }

    @Autowired
    public void setNextSessionManager(NextSessionManager manager) {
        this.manager = manager;
    }

    @Override
    protected final CommandResponse doAction(PARAM params) {
        return manager.updateAndReturn(params.getChannelId(), session -> doAction(params, session))
                .orElseGet(() -> localizationFactory.createResponse("errors.session.no-match").ephemeral(true));
    }

    protected abstract CommandResponse doAction(PARAM params, NextSession session);
}
