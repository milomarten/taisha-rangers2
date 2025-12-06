package com.github.milomarten.taisha_rangers2.command.autocomplete;

import org.apache.commons.lang3.StringUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Comparator;
import java.util.List;

public class FuzzyMatch {
    /**
     * A vague attempt at fuzzy matching by retrieving options with characters in the same relative order.
     * I think someone once said that this is a legitimate algorithm.
     * From the list of options, only those with characters in the same relative order as the search
     * string will be returned. For example, search "pe" matches both "peanut" and "private", but not
     * "post".
     * Within matches, they are ordered by the number of ignored characters it takes to see all characters
     * in the option. For instance, "peanut" has a score of 0, since the letters "p" and "e" are sequential and
     * at the beginning. Conversely, "private" has a score of 5, since the letters "p" and "e" are separated by
     * five letters (r, i, v, a, and t).
     * Empty string matches on all options.
     * @param soFar The search string
     * @param options The possible options
     * @return The matching options.
     */
    public static List<String> lazyFuzzyMatch(String soFar, List<String> options) {
        if (soFar.isEmpty()) { return options; }

        return options.stream()
                .map(opt -> Tuples.of(opt, getMatchWeight(soFar, opt)))
                .filter(p -> p.getT2() >= 0)
                .sorted(Comparator.comparing(Tuple2::getT2))
                .map(Tuple2::getT1)
                .toList();
    }

    /**
     * Get the match weight used for this algorithm.
     * If this number is non-negative, it represents the number of non-search letters
     * it took to match the provided option. As such, a bigger score usually indicates a more unlikely
     * match.
     * If result is negative, the search string did not wholly appear in the option.
     * @param soFar The search string
     * @param option The string to search in
     * @return The score of the match
     */
    public static int getMatchWeight(String soFar, String option) {
        if (soFar.isEmpty()) { return 0; }

        int counter = 0;
        int score = 0;
        char[] chars = StringUtils.deleteWhitespace(soFar).toCharArray();
        for (char c : option.toCharArray()) {
            if (c == chars[counter]) {
                counter++;
            } else {
                score++;
            }

            if (counter == chars.length) { break; }
        }

        return counter == chars.length ? score : -1;
    }
}
