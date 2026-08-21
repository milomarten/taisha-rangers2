package com.github.milomarten.taisha_rangers2.dice;

import org.apache.commons.rng.UniformRandomProvider;

public record Dice(Gem gem, Rarity rarity, Size size) {
    public static Dice random(UniformRandomProvider urp) {
        var rGem = random(Gem.values(), urp);
        var rRarity = random(Rarity.values(), urp);
        var rSize = random(Size.values(), urp);

        return new Dice(rGem, rRarity, rSize);
    }

    private static <T> T random(T[] arr, UniformRandomProvider urp) {
        return arr[urp.nextInt(0, arr.length)];
    }

    public String asString(boolean withArticle) {
        return (withArticle ? rarity.getArticle() + " " : "") + rarity.getText() + " " + size.getName() + "-sized " + gem.getName() + " dice";
    }
}
