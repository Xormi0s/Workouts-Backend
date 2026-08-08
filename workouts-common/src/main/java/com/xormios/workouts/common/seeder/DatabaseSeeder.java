package com.xormios.workouts.common.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final List<Seeder> seeders;

    @Override
    public void run(ApplicationArguments args) {
        seeders.forEach(Seeder::seed);
    }
}