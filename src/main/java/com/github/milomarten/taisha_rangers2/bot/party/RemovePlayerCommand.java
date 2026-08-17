package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("remove-player")
public class RemovePlayerCommand extends AbstractPartyAdminCommand<RemovePlayerCommand.Parameters> {
    private final GatewayDiscordClient gdc;

    public RemovePlayerCommand(@Autowired(required = false) GatewayDiscordClient gdc) {
        super("remove-player");
        this.gdc = gdc;
        setParameterParser(
                PartyIdentityParameters.parser(Parameters::new)
                        .withParameterField(
                                c -> c.getInteraction().getGuildId(),
                                (p, c) -> c.ifPresent(p::setGuildId))
                        .withParameterField(
                                "player",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToRemove
                        )
                        .withParameterField(
                                "alumnirole",
                                SnowflakeParameter.builder()
                                        .type(SnowflakeParameter.SnowflakeType.ROLE)
                                        .defaultValue(Snowflake.of(0))
                                        .build(),
                                Parameters::setAlumniRole
                        )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, Parameters params) {
        var worked = party.getPlayerIdentities().remove(params.playerToRemove) != null;
        if (worked) {

            deAssignRole(party.getName(), params.guildId, params.playerToRemove, party.getPing(), resolveFakeNull(params.alumniRole));

            return localizationFactory.createResponse(
                    "command.remove-player.response",
                    FormatUtils.pingUser(params.playerToRemove),
                    params.getPartyName()
            ).ephemeral(false);
        } else {
            return localizationFactory.createResponse("command.remove-player.error.player-doesnt-exist")
                    .ephemeral(true);
        }
    }

    private static Snowflake resolveFakeNull(Snowflake s) {
        return s.asLong() == 0 ? null : s;
    }

    private void deAssignRole(String partyName, Snowflake guildId, Snowflake userId, Snowflake oldRoleId, Snowflake alumniRoleMaybe) {
        if (gdc != null && guildId != null && (oldRoleId != null || alumniRoleMaybe != null)) {
            gdc.getMemberById(guildId, userId)
                    .flatMap(m -> {
                        Mono<Void> deleteRole, newRole;
                        if (oldRoleId != null) {
                            deleteRole = m.removeRole(oldRoleId, "Left " + partyName);
                        } else {
                            deleteRole = Mono.empty();
                        }
                        if (alumniRoleMaybe != null) {
                            newRole = m.addRole(alumniRoleMaybe, "Joined " + partyName + " alumni");
                        } else {
                            newRole = Mono.empty();
                        }

                        return Mono.when(deleteRole, newRole);
                    })
                    .subscribe();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private Snowflake guildId;
        private Snowflake playerToRemove;
        private Snowflake alumniRole;
    }
}
