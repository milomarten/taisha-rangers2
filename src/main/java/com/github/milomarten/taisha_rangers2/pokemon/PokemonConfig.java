package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Configuration
public class PokemonConfig {
    private static <T> List<T> parseCsv(String filename, Class<T> clazz, String profile) throws IOException {
        var resource = new ClassPathResource("pkmn/" + filename + ".csv");
        return new CsvToBeanBuilder<T>(new InputStreamReader(resource.getInputStream()))
                .withType(clazz)
                .withProfile(profile)
                .build()
                .parse();
    }

    private static <T> List<T> parseCsv(String filename, Class<T> clazz) throws IOException {
        return parseCsv(filename, clazz, "");
    }

    @Bean
    public List<PokemonSpecies> pokemonSpecies() throws IOException {
        return parseCsv("pokemon_species", PokemonSpecies.class);
    }

    @Bean
    public List<Ability> abilities() throws IOException {
        return parseCsv("abilities", Ability.class);
    }

    @Bean
    public List<Name> abilityNames() throws IOException {
        return parseCsv("ability_names", Name.class, "ability");
    }

    @Bean
    public List<Name> typeNames() throws IOException {
        return parseCsv("type_names", Name.class, "type");
    }

    @Bean
    public List<Name> pokemonSpeciesNames() throws IOException {
        return parseCsv("pokemon_species_names", Name.class, "pokemon_species");
    }

    @Bean
    public List<PokemonAbility> pokemonAbilities() throws IOException {
        return parseCsv("pokemon_abilities", PokemonAbility.class);
    }

    @Bean
    public List<PokemonAbility> pokemonType() throws IOException {
        return parseCsv("pokemon_types", PokemonAbility.class);
    }
}
