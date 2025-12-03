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

@Component("add-player")
public class AddPlayerCommand extends CommandSpec<AddPlayerCommand.Parameters> {
    private final PartyManager manager;

    public AddPlayerCommand(PartyManager manager) {
        super("add-player", "Add a player to a party");
        this.manager = manager;

        setParameterParser(
                PartyAdminParameters.parser(Parameters::new)
                        .withParameterField(
                                "player",
                                "The player to add to the party",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToAdd
                        )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        return manager.updatePartyAndReturn(params.getPartyName(), party -> {
            if (!Objects.equals(party.getDm(), params.getUserId())) {
                return CommandResponse.reply("Can only add players to your party!", true);
            }
            var worked = party.getPlayers().add(params.playerToAdd);
            if (worked) {
                return CommandResponse.reply(
                        String.format("Added %s to the %s party!", FormatUtils.pingUser(params.playerToAdd), params.getPartyName()),
                        false
                );
            } else {
                return CommandResponse.reply(
                        "That player is already in that party.",
                        true
                );
            }
        }).orElseGet(() -> CommandResponse.reply("What party???", true));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyAdminParameters {
        private Snowflake playerToAdd;
    }
}
