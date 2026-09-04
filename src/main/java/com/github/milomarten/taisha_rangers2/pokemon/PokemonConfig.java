package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

@Configuration
public class PokemonConfig {
    private static <T> List<T> parseCsv(String filename, Class<T> clazz, String profile) throws FileNotFoundException {
        var file = ResourceUtils.getFile("classpath:pkmn/" + filename + ".csv");
        return new CsvToBeanBuilder<T>(new FileReader(file))
                .withType(clazz)
                .withProfile(profile)
                .build()
                .parse();
    }

    private static <T> List<T> parseCsv(String filename, Class<T> clazz) throws FileNotFoundException {
        return parseCsv(filename, clazz, "");
    }

    @Bean
    public List<PokemonSpecies> pokemonSpecies() throws FileNotFoundException {
        return parseCsv("pokemon_species", PokemonSpecies.class);
    }

    @Bean
    public List<Ability> abilities() throws FileNotFoundException {
        return parseCsv("abilities", Ability.class);
    }

    @Bean
    public List<Name> abilityNames() throws FileNotFoundException {
        return parseCsv("ability_names", Name.class, "ability");
    }

    @Bean
    public List<Name> typeNames() throws FileNotFoundException {
        return parseCsv("type_names", Name.class, "type");
    }

    @Bean
    public List<Name> pokemonSpeciesNames() throws FileNotFoundException {
        return parseCsv("pokemon_species_names", Name.class, "pokemon_species");
    }

    @Bean
    public List<PokemonAbility> pokemonAbilities() throws FileNotFoundException {
        return parseCsv("pokemon_abilities", PokemonAbility.class);
    }

    @Bean
    public List<PokemonAbility> pokemonType() throws FileNotFoundException {
        return parseCsv("pokemon_types", PokemonAbility.class);
    }
}
