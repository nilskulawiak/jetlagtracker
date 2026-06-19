package com.nilskulawiak.jetlagtracker.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSession register(String email, String displayName, String rawPassword) {
        String normalizedEmail = email.toLowerCase().strip();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }
        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(displayName.strip());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return createSession(user);
    }

    @Transactional
    public UserSession login(String email, String rawPassword) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email.toLowerCase().strip(), rawPassword)
        );
        AppUser user = (AppUser) auth.getPrincipal();
        return createSession(user);
    }

    @Transactional
    public void logout(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
            .ifPresent(sessionRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> validateSession(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
            .filter(s -> s.getExpiresAt().isAfter(Instant.now()))
            .map(UserSession::getUser);
    }

    private UserSession createSession(AppUser user) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(UUID.randomUUID().toString());
        return sessionRepository.save(session);
    }
}
