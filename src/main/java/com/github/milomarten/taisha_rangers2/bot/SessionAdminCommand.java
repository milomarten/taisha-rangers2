package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Set;

public abstract class SessionAdminCommand<PARAM extends SessionAdminParams> extends LocalizedCommandSpec<PARAM> {
    @Getter private NextSessionManager manager;

    public SessionAdminCommand(String id) {
        super(id);
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Autowired
    public void setNextSessionManager(NextSessionManager manager) {
        this.manager = manager;
    }

    @Override
    protected final CommandResponse doAction(PARAM params) {
        return manager.updateAndReturn(params.getChannelId(), session -> {
            if (Objects.equals(session.getGm(), params.getUserId())) {
                return doAction(params, session);
            } else {
                return localizationFactory.createResponse("errors.session.no-access")
                        .ephemeral(true);
            }
        }).orElseGet(() -> localizationFactory.createResponse("errors.session.no-match").ephemeral(true));
    }

    protected abstract CommandResponse doAction(PARAM params, NextSession session);
}
