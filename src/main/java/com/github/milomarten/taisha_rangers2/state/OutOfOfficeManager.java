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
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class OutOfOfficeManager {
    private static final String KEY = "ooo";
    private static final TypeReference<List<OutOfOffice>> TYPE =
            new TypeReference<>() {};
    private static final TypeReference<Map<Snowflake, List<OutOfOffice>>> TYPE_2 =
            new TypeReference<>() {};

    private final Map<Snowflake, List<OutOfOffice>> outOfOffices
            = Collections.synchronizedMap(new HashMap<>());
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
        this.persister.load(KEY, TYPE_2)
                .onErrorResume(ex -> {
                    return this.persister.load(KEY, TYPE)
                            .map(l -> l.stream()
                                    .collect(Collectors.groupingBy(OutOfOffice::getPlayer)));
                })
                .doOnSuccess(map -> {
                    if (map != null) {
                        outOfOffices.putAll(map);
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
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end is before start");
        }

        var list = outOfOffices.computeIfAbsent(who, k -> new ArrayList<>());
        var ooo = new OutOfOffice(who, start, end);
        var intersecting = getIntersectingOOOs(list, start, end);
        var sequential = getSequentialOOOs(list, start, end);
        if (sequential.isEmpty() && intersecting.isEmpty()) {
            list.add(ooo);
        } else {
            var resolved = spliceInNewOOO(intersecting, sequential, ooo);
            list.removeAll(intersecting);
            list.removeAll(sequential);
            list.add(resolved);
        }

        persist();
    }

    private List<OutOfOffice> getIntersectingOOOs(List<OutOfOffice> ooos, LocalDate start, LocalDate end) {
        if (ooos != null) {
            return ooos.stream()
                    .filter(ooo -> {
                        return start.isBefore(ooo.getEnd())
                                || end.isAfter(ooo.getStart());
                    })
                    .toList();
        } else {
            return List.of();
        }
    }

    private List<OutOfOffice> getSequentialOOOs(List<OutOfOffice> ooos, LocalDate start, LocalDate end) {
        if (ooos != null) {
            return ooos.stream()
                    .filter(ooo -> {
                        return areDatesInSequence(ooo.getEnd(), start)
                                || areDatesInSequence(end, ooo.getStart());
                    })
                    .toList();
        } else {
            return List.of();
        }
    }

    private boolean areDatesInSequence(LocalDate one, LocalDate two) {
        return Objects.equals(one.plusDays(1), two);
    }

    OutOfOffice spliceInNewOOO(List<OutOfOffice> collisions, List<OutOfOffice> sequences, OutOfOffice additional) {
        var timesList = Stream.concat(collisions.stream(), sequences.stream())
                .<LocalDate>mapMulti((ooo, eater) -> {
                    eater.accept(ooo.getStart());
                    eater.accept(ooo.getEnd());
                })
                .sorted()
                .toList();
        return new OutOfOffice(additional.getPlayer(), timesList.getFirst(), timesList.getLast());
    }

    /**
     * Clean up the out of office list
     * Remove anything on the OOO list that ended in the past
     */
    @Scheduled(cron = "0 0 6 * * MON", zone = "America/Chicago")
    public void cleanDates() {
        outOfOffices
                .values()
                .forEach(ooos -> ooos.removeIf(
                        ooo -> ooo.getEnd().isBefore(LocalDate.now())));

        persist();
    }

    /**
     * Check who is out on a certain day
     * @param when The day to check
     * @return The list of people out on that day.
     */
    public List<Snowflake> whoIsOutOn(LocalDate when) {
        return outOfOffices.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(ooo -> testInDate(when, ooo.getStart(), ooo.getEnd()))
                .map(OutOfOffice::getPlayer)
                .distinct()
                .toList();
    }

    /**
     * Get any OOOs that are between now and within the provided scope.
     * Any OOOs that are have an end date including or after today's date, or a start date before or including
     * the now + scope, are included.
     * @param people The relevant people to see
     * @param scope The number of days in the future to search
     * @return The OOOs that are within the upcoming period
     */
    public List<OutOfOffice> getUpcoming(Set<Snowflake> people, Period scope) {
        var now = LocalDate.now();
        var future = now.plus(scope);
        return outOfOffices.entrySet()
                .stream()
                .filter(entry -> people.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(ooo -> {
                    var isEndDateAfterNow = ooo.getEnd().compareTo(now) >= 0;
                    var isStartDateBeforeFuture = ooo.getStart().compareTo(future) <= 0;
                    return isEndDateAfterNow || isStartDateBeforeFuture;
                })
                .toList();
    }
}
