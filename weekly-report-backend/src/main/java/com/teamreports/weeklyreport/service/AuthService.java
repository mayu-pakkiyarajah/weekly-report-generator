package com.teamreports.weeklyreport.service;

import com.teamreports.weeklyreport.dto.auth.AuthResponse;
import com.teamreports.weeklyreport.dto.auth.LoginRequest;
import com.teamreports.weeklyreport.dto.auth.RegisterRequest;
import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.entity.enums.Role;
import com.teamreports.weeklyreport.exception.DuplicateResourceException;
import com.teamreports.weeklyreport.repository.UserRepository;
import com.teamreports.weeklyreport.security.JwtService;
import com.teamreports.weeklyreport.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        // Public self-registration is always TEAM_MEMBER; manager accounts are created
        // deliberately via the admin user-management endpoint.
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.TEAM_MEMBER)
                .active(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateAccessToken(UserPrincipal.from(user));
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished unexpectedly."));

        String token = jwtService.generateAccessToken(UserPrincipal.from(user));
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
