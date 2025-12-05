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

    List<OutOfOffice> splitOutOOO(OutOfOffice original, OutOfOffice split) {
        // 12/5-12/8 - 12/5-12/8 = []
        // 12/5-12/8 - 12/2-12/8 = []
        // 12/5-12/8 - 12/5-12/10 = []
        // 12/5-12/8 - 12/2-12/10 = []
        if (split.getStart().compareTo(original.getStart()) <= 0 && split.getEnd().compareTo(original.getEnd()) >= 0) {
            // special handling if the split completely encapsulates the original. The whole thing goes.
            return List.of();
        }
        // 12/5-12/8 - 12/6-12/7 = [12/5, 12/8]
        else if (split.getStart().isAfter(original.getStart()) &&
                split.getEnd().isBefore(original.getEnd())
        ) {
            // special handling if the split is completely within the original, becomes 2 periods.
            return List.of(
                    new OutOfOffice(original.getPlayer(), original.getStart(), split.getStart().minusDays(1)),
                    new OutOfOffice(original.getPlayer(), split.getEnd().plusDays(1), original.getEnd())
            );
        }
        // 12/5-12/8 - 12/7-12/10 == 12/5-12/6
        // 12/5-12/8 - 12/7-12/8 == 12/5-12/6
        else if (split.getStart().isAfter(original.getStart())) {
            return List.of(
                    new OutOfOffice(original.getPlayer(), original.getStart(), split.getStart().minusDays(1))
            );
        }
        // 12/5-12/8 - 12/1-12/6 == 12/7-12/8
        // 12/5-12/8 - 12/5-12/6 == 12/7-12/8
        else if (split.getEnd().isBefore(original.getEnd())) {
            return List.of(
                    new OutOfOffice(original.getPlayer(), split.getEnd().plusDays(1), original.getEnd())
            );
        } else {
            //Times do not coincide.
            return List.of(original);
        }
    }

    /**
     * Remove one of your out dates
     * @param who The person to edit
     * @param date A date in the vacation block they want to delete
     * @return True, if at least one date was removed.
     */
    public boolean removeOutDate(Snowflake who, LocalDate date) {
        if (outOfOffices.containsKey(who)) {
            // one day, this should be a bit more complex.
            var resolved = outOfOffices.get(who)
                    .stream()
                    .filter(ooo -> !testInDate(date, ooo.getStart(), ooo.getEnd()))
                    .toList();
            outOfOffices.put(who, resolved);
            persist();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the dates you are out of office for
     * @param who The person to view
     * @return A list of their upcoming out of office dates
     */
    public List<OutOfOffice> getOutDatesFor(Snowflake who) {
        return outOfOffices.getOrDefault(who, Collections.emptyList());
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

    private static <T> List<T> of(T... items) {
        return new ArrayList<>(List.of(items));
    }
}
