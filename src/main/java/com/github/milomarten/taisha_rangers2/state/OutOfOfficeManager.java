package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.taisha_rangers2.persistence.JsonFilePersister;
import com.github.milomarten.taisha_rangers2.persistence.NoOpPersister;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class OutOfOfficeManager {
    private static final String KEY = "ooo";
    private static final TypeReference<List<OutOfOffice>> TYPE =
            new TypeReference<>() {};

    private final Set<OutOfOffice> outOfOffices
            = Collections.synchronizedSet(new HashSet<>());
    private final Persister persister;

    @Autowired
    public OutOfOfficeManager(
            @Value("${persistence.session-manager.base-path:}") String path,
            ObjectMapper om) {
        this.persister = StringUtils.isEmpty(path) ?
                new NoOpPersister() :
                new JsonFilePersister(path, om);
    }

    @PostConstruct
    public void init() {
        this.persister.load(KEY, TYPE)
                .doOnSuccess(list -> {
                    if (list != null) {
                        outOfOffices.addAll(list);
                    }
                })
                .subscribe();
    }

    private void persist() {
        this.persister.persist(KEY, this.outOfOffices)
                .onErrorResume(ex -> {
                    log.error("Error persisting OOOs", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private boolean testInDate(LocalDate test, LocalDate left, LocalDate right) {
        return !test.isBefore(left) && !test.isAfter(right);
    }

    /**
     * Add a date that someone will be out for
     * @param who The person who will be out
     * @param start The start time of their vacation
     * @param end The end time of their vacation
     */
    public void addOutDate(Snowflake who, LocalDate start, LocalDate end) {
        outOfOffices.add(new OutOfOffice(who, start, end));
        persist();
    }

    /**
     * Remove one of your out dates
     * @param who The person to edit
     * @param date A date in the vacation block they want to delete
     * @return True, if at least one date was removed.
     */
    public boolean removeOutDate(Snowflake who, LocalDate date) {
        boolean worked = outOfOffices.removeIf(ooo ->
                ooo.getPlayer().equals(who) && testInDate(date, ooo.getStart(), ooo.getEnd()));

        if (worked) {
            persist();
        }

        return worked;
    }

    /**
     * Get the dates you are out of office for
     * @param who The person to view
     * @return A list of their upcoming out of office dates
     */
    public List<OutOfOffice> getOutDatesFor(Snowflake who) {
        return outOfOffices.stream()
                .filter(ooo -> ooo.getPlayer().equals(who))
                .toList();
    }

    /**
     * Clean up the out of office list
     * Remove anything on the OOO list that ended in the past
     */
    @Scheduled(cron = "0 0 6 * * MON", zone = "America/Chicago")
    public void cleanDates() {
        boolean worked = outOfOffices
                .removeIf(ooo -> ooo.getEnd().isBefore(LocalDate.now()));

        if (worked) {
            persist();
        }
    }

    /**
     * Check who is out on a certain day
     * @param when The day to check
     * @return The list of people out on that day.
     */
    public List<Snowflake> whoIsOutOn(LocalDate when) {
        return outOfOffices.stream()
                .filter(ooo -> testInDate(when, ooo.getStart(), ooo.getEnd()))
                .map(OutOfOffice::getPlayer)
                .distinct()
                .toList();
    }
}
