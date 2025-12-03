package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import discord4j.common.util.Snowflake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyAdminParameters {
    private String partyName;
    private Snowflake userId;

    public static PojoParameterParser<PartyAdminParameters> parser() {
        return new PojoParameterParser<>(PartyAdminParameters::new)
                .withParameterField("party-name", "Name of party", StringParameter.REQUIRED, PartyAdminParameters::setPartyName)
                .withParameterField(PojoParameterParser.userId(), PartyAdminParameters::setUserId);
    }

    public static <T extends PartyAdminParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
        return new PojoParameterParser<>(constructor)
                .withParameterField("party-name", "Name of party", StringParameter.REQUIRED, PartyAdminParameters::setPartyName)
                .withParameterField(PojoParameterParser.userId(), PartyAdminParameters::setUserId);
    }
}
