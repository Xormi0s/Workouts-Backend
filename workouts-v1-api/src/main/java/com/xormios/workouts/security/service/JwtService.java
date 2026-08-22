package com.xormios.workouts.security.service;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.auth.Role;
import com.xormios.workouts.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    public String generateAccessToken(ApplicationUser applicationUser) {
        Instant now = Instant.now();
        Instant expired = now.plusMillis(jwtProperties.getAccessTokenExpirationMs());

        List<String> roles = applicationUser.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .subject(applicationUser.getUsername())
                .issuer("Xormios")
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expired))
                .signWith(getSecretKey())
                .compact();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        boolean valid = true;
        Claims claims = getClaimsFromToken(token);

        if(!claims.getSubject().equals(userDetails.getUsername())) {
            valid = false;
        }

        if (!claims.getExpiration().after(new Date())) {
            valid = false;
        }

        return valid;
    }
}
