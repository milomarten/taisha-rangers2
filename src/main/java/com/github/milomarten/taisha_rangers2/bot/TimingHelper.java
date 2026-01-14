package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Component
@ConfigurationProperties("reminder")
@Getter @Setter
public class TimingHelper {
    private Duration farOffSessionThreshold;
    private Duration latestSubmitStatusOffset;
    private Duration submitStatusReminderOffset;
    private Duration sessionStartReminderOffset;
    private Duration doneReminderOffset;
    private Duration startTimeCalculationFakeOffset;

    public ZonedDateTime getAnnouncementTime(NextSession session) {
        return session.getProposedStartTime().plus(farOffSessionThreshold);
    }

    public boolean isFarOffSession(NextSession session) {
        var now = ZonedDateTime.now();
        var announcementTime = getAnnouncementTime(session);

        return now.isBefore(announcementTime);
    }

    public ZonedDateTime getLatestStatusSubmitTime(NextSession session) {
        return session.getProposedStartTime()
                .plus(latestSubmitStatusOffset);
    }

    public ZonedDateTime getGentleReminderTime(NextSession session) {
        return session.getProposedStartTime()
                .plus(submitStatusReminderOffset);
    }

    public ZonedDateTime getSessionStartReminderTime(NextSession session) {
        return session.getStartTime().plus(sessionStartReminderOffset);
    }

    public ZonedDateTime getDoneReminderTime(NextSession session) {
        return session.getStartTime().plus(doneReminderOffset);
    }

    public ZonedDateTime getDayOfPingTime(NextSession session) {
        ZoneId timezone;
        if (session.getParty().getUsualTime() != null) {
            timezone = session.getParty().getUsualTime().getTimezone();
        } else {
            timezone = ZoneId.of("America/New_York");
        }
        var pingTime = session.getStartTime()
                .withZoneSameInstant(timezone)
                .with(LocalTime.of(10, 0));
        if (pingTime.isAfter(session.getStartTime())) {
            return pingTime.minusDays(1);
        } else {
            return pingTime;
        }
    }
}
