package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NextSessionTest {

    /*
    NextSession(
    channel=Snowflake{1169683408373424228},
    party=Party(name=rangers,
    dm=Snowflake{248612704019808258},
    players=[
        Snowflake{181858214642450432},
        Snowflake{105024512612909056},
        Snowflake{105304527179124736},
        Snowflake{252289670522601472}],
    ping=Snowflake{1169680518531518544},
    usualTime=PartyTime(dayOfWeek=TUESDAY, timeOfDay=19:00, timezone=America/Chicago)),
    playerResponses={
        Snowflake{105304527179124736}=PlayerResponse(player=Snowflake{105304527179124736}, state=YES, afterTime=null, beforeTime=null),
        Snowflake{252289670522601472}=PlayerResponse(player=Snowflake{252289670522601472}, state=YES, afterTime=null, beforeTime=null),
        Snowflake{105024512612909056}=PlayerResponse(player=Snowflake{105024512612909056}, state=YES, afterTime=null, beforeTime=null),
        Snowflake{181858214642450432}=PlayerResponse(player=Snowflake{181858214642450432}, state=YES, afterTime=null, beforeTime=null)},
    proposedStartTime=2025-12-10T01:00Z,
    startTime=null)
     */
    @Test
    public void testAllPlayersRespondedYes()
    {
        var party = new Party();
        party.setName("rangers");
        party.setDm(Snowflake.of(248612704019808258L));
        party.setPing(Snowflake.of(1169680518531518544L));
        party.setUsualTime(new PartyTime(DayOfWeek.TUESDAY, LocalTime.of(19, 0), ZoneId.of("America/Chicago")));
        party.setPlayers(Set.of(
                Snowflake.of(181858214642450432L),
                Snowflake.of(105024512612909056L),
                Snowflake.of(105304527179124736L),
                Snowflake.of(252289670522601472L)
        ));

        var session = new NextSession(
                Snowflake.of(1169683408373424228L),
                party,
                ZonedDateTime.parse("2025-12-10T01:00Z")
        );

        var responses = session.getPlayerResponses();
        addYes(responses, 181858214642450432L);
        addYes(responses, 105024512612909056L);
        addYes(responses, 105304527179124736L);
        addYes(responses, 252289670522601472L);

        assertTrue(session.allPlayersRespondedYes());
//        assertEquals("NextSession(channel=Snowflake{1169683408373424228}, party=Party(name=rangers, dm=Snowflake{248612704019808258}, players=[Snowflake{181858214642450432}, Snowflake{105024512612909056}, Snowflake{105304527179124736}, Snowflake{252289670522601472}], ping=Snowflake{1169680518531518544}, usualTime=PartyTime(dayOfWeek=TUESDAY, timeOfDay=19:00, timezone=America/Chicago)), playerResponses={Snowflake{105304527179124736}=PlayerResponse(player=Snowflake{105304527179124736}, state=YES, afterTime=null, beforeTime=null), Snowflake{252289670522601472}=PlayerResponse(player=Snowflake{252289670522601472}, state=YES, afterTime=null, beforeTime=null), Snowflake{105024512612909056}=PlayerResponse(player=Snowflake{105024512612909056}, state=YES, afterTime=null, beforeTime=null), Snowflake{181858214642450432}=PlayerResponse(player=Snowflake{181858214642450432}, state=YES, afterTime=null, beforeTime=null)}, proposedStartTime=2025-12-10T01:00Z, startTime=null)", session.toString());
    }

    private void addYes(Map<Snowflake, PlayerResponse> map, long id) {
        var idSnowflake = Snowflake.of(id);
        var pr = new PlayerResponse(idSnowflake);
        pr.yes(null, null);
        map.put(idSnowflake, pr);
    }
}