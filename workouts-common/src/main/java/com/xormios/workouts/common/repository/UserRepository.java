package com.xormios.workouts.common.repository;

import com.xormios.workouts.common.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser,Long> {

    Optional<ApplicationUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
