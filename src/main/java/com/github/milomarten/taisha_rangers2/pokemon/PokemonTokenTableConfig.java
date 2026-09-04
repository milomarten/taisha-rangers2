package com.github.milomarten.taisha_rangers2.pokemon;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.StringTerm;
import com.github.milomarten.table.RandomlySelected;
import com.github.milomarten.table.UnweightedTable;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class PokemonTokenTableConfig {
    public static final String PREFIX = "RS_";

    private Map<Integer, String> getEnglishNameLookup(List<Name> names) {
        return names.stream()
                .filter(n -> n.getLanguageId() == 9)
                .collect(Collectors.toMap(
                        Name::getId,
                        Name::getName
                ));
    }

    private RandomlySelected<DiceMathTerm> wrap(Stream<String> stream) {
        var unweighted = new UnweightedTable<DiceMathTerm>();
        stream.forEach(n -> unweighted.addEntry(new StringTerm(n)));
        return unweighted;
    }

    @Bean(name = PREFIX + "ability")
    public RandomlySelected<DiceMathTerm> randomAbilities(List<Name> abilityNames) {
        var names = abilityNames.stream()
                .filter(n -> n.getLanguageId() == 9)
                .map(Name::getName);
        return wrap(names);
    }

    @Bean
    public Map<Integer, String> pokemonEnglishNameLookup(List<Name> pokemonSpeciesNames) {
        return getEnglishNameLookup(pokemonSpeciesNames);
    }

    @Bean(name = PREFIX + "pokemon")
    public RandomlySelected<DiceMathTerm> randomPokemon(List<PokemonSpecies> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var pkmnNames = species.stream()
                .filter(s -> !s.isLegendary() && !s.isMythical())
                .map(ps -> pokemonEnglishNameLookup.get(ps.getId()))
                .filter(Objects::nonNull);
        return wrap(pkmnNames);
    }

    @Bean
    public List<Evolution> getEvolutionChain(List<PokemonSpecies> species) {
        var evolutions = species.stream()
                .collect(Collectors.toMap(
                        PokemonSpecies::getId,
                        Evolution::new
                ));

        for (var s : evolutions.values()) {
            if (s.self.getEvolvesFromSpeciesId() != null) {
                var preEvolution = evolutions.get(s.self.getEvolvesFromSpeciesId());
                if (preEvolution != null) {
                    s.preEvolution = preEvolution.self;
                    preEvolution.evolutions.add(s.self);
                }
            }
        }

        return List.copyOf(evolutions.values());
    }

    @Data
    public static class Evolution {
        private final PokemonSpecies self;
        private PokemonSpecies preEvolution;
        private List<PokemonSpecies> evolutions = new ArrayList<>();
    }

    @Bean(name = PREFIX + "immute")
    public RandomlySelected<DiceMathTerm> randomImmute(List<Evolution> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var candidates = species.stream()
                .filter(s -> !s.self.isLegendary() && !s.self.isMythical())
                .filter(s -> s.preEvolution == null && s.evolutions.isEmpty())
                .map(s -> pokemonEnglishNameLookup.get(s.self.getId()))
                .filter(Objects::nonNull);
        return wrap(candidates);
    }

    @Bean(name = PREFIX + "nfe")
    public RandomlySelected<DiceMathTerm> randomNotFullyEvolved(List<Evolution> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var candidates = species.stream()
                .filter(s -> !s.self.isLegendary() && !s.self.isMythical())
                .filter(s -> !s.evolutions.isEmpty())
                .map(s -> pokemonEnglishNameLookup.get(s.self.getId()))
                .filter(Objects::nonNull);
        return wrap(candidates);
    }

    @Bean(name = PREFIX + "fe")
    public RandomlySelected<DiceMathTerm> randomFullyEvolved(List<Evolution> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var candidates = species.stream()
                .filter(s -> !s.self.isLegendary() && !s.self.isMythical())
                .filter(s -> s.preEvolution != null && s.evolutions.isEmpty())
                .map(s -> pokemonEnglishNameLookup.get(s.self.getId()))
                .filter(Objects::nonNull);
        return wrap(candidates);
    }

    @Bean(name = PREFIX + "me")
    public RandomlySelected<DiceMathTerm> randomMiddleEvolved(List<Evolution> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var candidates = species.stream()
                .filter(s -> !s.self.isLegendary() && !s.self.isMythical())
                .filter(s -> s.preEvolution != null && !s.evolutions.isEmpty())
                .map(s -> pokemonEnglishNameLookup.get(s.self.getId()))
                .filter(Objects::nonNull);
        return wrap(candidates);
    }

    @Bean(name = PREFIX + "ue")
    public RandomlySelected<DiceMathTerm> randomUnevolved(List<Evolution> species, Map<Integer, String> pokemonEnglishNameLookup) {
        var candidates = species.stream()
                .filter(s -> !s.self.isLegendary() && !s.self.isMythical())
                .filter(s -> s.preEvolution == null && !s.evolutions.isEmpty())
                .map(s -> pokemonEnglishNameLookup.get(s.self.getId()))
                .filter(Objects::nonNull);
        return wrap(candidates);
    }

    @Bean(name = PREFIX + "type")
    public RandomlySelected<DiceMathTerm> randomType(List<Name> typeNames) {
        var names = typeNames.stream()
                .filter(n -> n.getLanguageId() == 9)
                .map(Name::getName);
        return wrap(names);
    }

    // future enhancement - by type?
}
