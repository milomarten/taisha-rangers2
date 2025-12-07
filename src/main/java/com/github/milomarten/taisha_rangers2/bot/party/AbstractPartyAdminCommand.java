package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.exception.NotDM;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractPartyAdminCommand<PARAM extends PartyIdentityParameters> extends LocalizedCommandSpec<PARAM> {
    @Getter private PartyManager partyManager;

    @Autowired
    public void setPartyManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public AbstractPartyAdminCommand(String id) {
        super(id);
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    protected CommandResponse doAction(PARAM params) {
        return partyManager.updatePartyAndReturn(params.getPartyName(), party -> {
            if (Objects.equals(party.getDm(), params.getUserId())) {
                return doProtectedPartyAction(party, params);
            } else {
                throw new NotDM();
            }
        }).orElseGet(() -> {
            return localizationFactory.createResponse("errors.party.no-match", params.getPartyName());
        });
    }

    protected abstract CommandResponse doProtectedPartyAction(Party party, PARAM params);
}
