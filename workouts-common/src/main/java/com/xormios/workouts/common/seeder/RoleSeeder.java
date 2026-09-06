package com.xormios.workouts.common.seeder;

import com.xormios.workouts.common.constant.RoleNames;
import com.xormios.workouts.common.entity.auth.Role;
import com.xormios.workouts.common.repository.auth.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements Seeder {

    private final RoleRepository roleRepository;

    @Override
    public void seed() {
        List.of(RoleNames.ROLE_USER).forEach(name ->
                roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(new Role(null, name)))
        );
    }
}