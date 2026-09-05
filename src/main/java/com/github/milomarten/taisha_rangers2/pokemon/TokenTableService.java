package com.github.milomarten.taisha_rangers2.pokemon;

import com.github.milomarten.dice.TokenTable;
import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.table.RandomlySelected;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenTableService {
    private final Map<String, RandomlySelected<DiceMathTerm>> randomTokenResolvers;

    public TokenTable getTokenTable(UniformRandomProvider randomness) {
        var tt = new TokenTable();
        randomTokenResolvers.forEach((token, random) -> {
            tt.addRandomlySelected(token, random, randomness);
        });
        return tt;
    }
}
