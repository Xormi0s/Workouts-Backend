package com.xormios.workouts.common.repository.auth;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByTokenHash(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.applicationUser = :user and rt.revoked = false")
    int revokeAllTokensByApplicationUser(@Param("user") ApplicationUser applicationUser);
}
