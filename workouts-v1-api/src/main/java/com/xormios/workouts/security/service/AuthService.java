package com.xormios.workouts.security.service;

import com.xormios.workouts.common.entity.ApplicationUser;
import com.xormios.workouts.common.entity.auth.RefreshToken;
import com.xormios.workouts.common.constant.RoleNames;
import com.xormios.workouts.common.entity.auth.Role;
import com.xormios.workouts.common.repository.RoleRepository;
import com.xormios.workouts.common.repository.UserRepository;
import com.xormios.workouts.security.config.AccountLockoutProperties;
import com.xormios.workouts.security.config.JwtProperties;
import com.xormios.workouts.security.dto.*;
import com.xormios.workouts.security.exception.TokenRefreshException;
import com.xormios.workouts.security.exception.UsernameAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional(dontRollbackOn = BadCredentialsException.class)
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AccountLockoutProperties accountLockoutProperties;

    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.existsByUsername(registerRequest.username())){
            throw new UsernameAlreadyExistsException("username already exists: " + registerRequest.username());
        }

        Role role = roleRepository.findByName(RoleNames.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleNames.ROLE_USER)));

        ApplicationUser user = new ApplicationUser();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setExpired(false);
        user.setCredentialsExpired(false);
        user.getRoles().add(role);

        userRepository.save(user);
        return issueToken(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        ApplicationUser user = userRepository.findByUsername(loginRequest.username()).orElse(null);

        if(user != null){
            unlockIfExpired(user);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        } catch (BadCredentialsException e) {
            if(user != null){
                registerFailedLogin(user);
            }
            throw e;
        }

        resetFailedAttempts(user);
        return issueToken(user);
    }


    private AuthResponse issueToken(ApplicationUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, (jwtProperties.getAccessTokenExpirationMs() / 1000));
    }

    public AuthResponse refresh(RefreshRequest refreshRequest) {
        RefreshToken existing = refreshTokenService.findByToken(refreshRequest.refreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        if(existing.isRevoked()){
            refreshTokenService.revokeAllForUser(existing.getApplicationUser());
            throw new TokenRefreshException("Refresh token has been revoked");
        }

        if(existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Refresh token has expired");
        }

        existing.setRevoked(true);
        refreshTokenService.save(existing);

        return issueToken(existing.getApplicationUser());
    }

    public void logout(String username, LogoutRequest logoutRequest) {
        refreshTokenService.findByToken(logoutRequest.refreshToken())
                .ifPresent(refreshToken -> {
                    if(!refreshToken.getApplicationUser().getUsername().equals(username)){
                        throw new AccessDeniedException("This refresh token does not belong to the authenticated user");
                    }
                    refreshToken.setRevoked(true);
                    refreshTokenService.save(refreshToken);
                });
    }

    private void unlockIfExpired(ApplicationUser user){
        if(user.isLocked() && user.getLockedAt() != null && user.getLockedAt().plusMinutes(accountLockoutProperties.getLockoutDurationMinutes()).isBefore(LocalDateTime.now())){
            user.setLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);
            userRepository.save(user);
        }
    }

    private void registerFailedLogin(ApplicationUser user){
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= accountLockoutProperties.getMaxAttempts()) {
            user.setLocked(true);
            user.setLockedAt(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(ApplicationUser user){
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    public void logoutAll(String username) {
        ApplicationUser user = userRepository.findByUsername(username).
                orElseThrow(() -> new IllegalArgumentException("Username not found: " + username));
        refreshTokenService.revokeAllForUser(user);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username not found: " + username));

        if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())){
            throw new BadCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenService.revokeAllForUser(user);
    }
}
