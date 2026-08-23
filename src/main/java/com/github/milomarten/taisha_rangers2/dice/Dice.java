package com.github.milomarten.taisha_rangers2.dice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

@Data
public class Dice {
    Gem gem;
    Rarity rarity;
    Size size;
    @JsonIgnore UniformRandomProvider urp = RandomSource.MT.create();

    public static Dice random() {
        var dice = new Dice();
        dice.gem = random(Gem.values(), dice.urp);
        dice.rarity = random(Rarity.values(), dice.urp);
        dice.size = random(Size.values(), dice.urp);

        return dice;
    }

    private static <T> T random(T[] arr, UniformRandomProvider urp) {
        return arr[urp.nextInt(0, arr.length)];
    }

    public String asString(boolean withArticle) {
        return (withArticle ? rarity.getArticle() + " " : "") + rarity.getText() + " " + size.getName() + "-sized " + gem.getName() + " dice";
    }
}
