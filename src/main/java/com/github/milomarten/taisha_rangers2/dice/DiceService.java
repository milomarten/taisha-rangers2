package com.github.milomarten.taisha_rangers2.dice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DiceService {
    public static final TypeReference<Map<String, Dice>> DICE_MAP = new TypeReference<>() { };
    public static final String DICE_KEY = "dice";

    private final Map<String, Dice> dice = new ConcurrentHashMap<>();
    private final Persister persister;

    @PostConstruct
    public void load() {
        persister.load(DICE_KEY, DICE_MAP)
                .doOnSuccess(map -> {
                    if (map != null) {
                        dice.putAll(map);
                    }
                })
                .block();
    }

    private void persist() {
        persister.persist(DICE_KEY, dice).subscribe();
    }

    public Dice getDice(String id) {
        var diceToReturn = _getDice(id);
        persist();
        return diceToReturn;
    }

    private Dice _getDice(String id) {
        return dice.computeIfAbsent(id, i -> Dice.random());
    }

    public int rollDice(String id, int origin, int upperBound) {
        return _getDice(id).urp.nextInt(origin, upperBound);
    }

    public DiceJailResult jail(String id) {
        var newDice = Dice.random();
        var oldDice = Objects.requireNonNullElseGet(dice.put(id, newDice), Dice::random);

        persist();
        return new DiceJailResult(oldDice, newDice);
    }

    public record DiceJailResult(Dice oldOne, Dice newOne) {}
}
