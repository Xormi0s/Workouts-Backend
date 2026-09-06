package com.xormios.workouts.common.repository.auth;

import com.xormios.workouts.common.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser,Long> {

    @Query("SELECT u FROM ApplicationUser u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<ApplicationUser> findByUsername(@Param("username") String username);

    boolean existsByUsername(String username);
}
