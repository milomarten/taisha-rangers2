package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component("remove-player")
public class RemovePlayerCommand extends CommandSpec<RemovePlayerCommand.Parameters> {
    private final PartyManager manager;

    public RemovePlayerCommand(PartyManager manager) {
        super("remove-player", "Remove a player from a party");
        this.manager = manager;

        setParameterParser(
                PartyAdminParameters.parser(Parameters::new)
                        .withParameterField(
                                "player",
                                "The player to remove from the party",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToRemove
                        )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        return manager.updatePartyAndReturn(params.getPartyName(), party -> {
            if (!Objects.equals(party.getDm(), params.getUserId())) {
                return CommandResponse.reply("Can only remove players from your party!", true);
            }
            var worked = party.getPlayers().remove(params.playerToRemove);
            if (worked) {
                return CommandResponse.reply(
                        String.format("Removed %s from the %s party.", FormatUtils.pingUser(params.playerToRemove), params.getPartyName()),
                        false
                );
            } else {
                return CommandResponse.reply(
                        "That player was not in the party.",
                        true
                );
            }
        }).orElseGet(() -> CommandResponse.reply("What party???", true));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyAdminParameters {
        private Snowflake playerToRemove;
    }
}
