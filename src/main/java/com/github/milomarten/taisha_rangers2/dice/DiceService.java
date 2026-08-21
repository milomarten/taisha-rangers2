package com.github.milomarten.taisha_rangers2.dice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DiceService {
    public static final TypeReference<Map<String, Dice>> DICE_MAP = new TypeReference<>() { };
    public static final String DICE_KEY = "dice";

    private final Map<String, Dice> dice = new ConcurrentHashMap<>();
    private final Map<String, UniformRandomProvider> randomness = new ConcurrentHashMap<>();
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

    public Dice getDice(String id) {
        var diceToReturn = dice.computeIfAbsent(id, i -> {
            var r = randomness.computeIfAbsent(id, j -> RandomSource.MT.create());
            return Dice.random(r);
        });
        persister.persist(DICE_KEY, dice).subscribe();
        return diceToReturn;
    }

    public int rollDice(String id, int origin, int upperBound) {
        return randomness.computeIfAbsent(id, j -> RandomSource.MT.create())
                .nextInt(origin, upperBound);
    }

    public DiceJailResult jail(String id) {
        var oldDice = getDice(id);
        var newRandomness = RandomSource.MT.create();
        var newDice = Dice.random(newRandomness);

        dice.put(id, newDice);
        randomness.put(id, newRandomness);
        persister.persist(DICE_KEY, dice).subscribe();
        return new DiceJailResult(oldDice, newDice);
    }

    public record DiceJailResult(Dice oldOne, Dice newOne) {}
}
