package com.nilskulawiak.jetlagtracker.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private AppUserRepository userRepository;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void registerCreatesUserAndSession() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSession session = userService.register("user@example.com", "User", "password");

        assertThat(session.getSessionToken()).isNotNull();
        assertThat(session.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(session.getUser().getDisplayName()).isEqualTo("User");
    }

    @Test
    void registerNormalizesEmail() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSession session = userService.register("  USER@EXAMPLE.COM  ", "User", "password");

        assertThat(session.getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("taken@example.com", "User", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void loginReturnsSessionForValidCredentials() {
        AppUser user = userWithId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSession session = userService.login("user@example.com", "password");

        assertThat(session.getUser()).isSameAs(user);
        assertThat(session.getSessionToken()).isNotNull();
    }

    @Test
    void validateSessionReturnsUserForValidToken() {
        AppUser user = userWithId(UUID.randomUUID());
        UserSession session = sessionWithExpiry(user, Instant.now().plusSeconds(3600));
        when(sessionRepository.findBySessionToken("valid-token")).thenReturn(Optional.of(session));

        Optional<AppUser> result = userService.validateSession("valid-token");

        assertThat(result).contains(user);
    }

    @Test
    void validateSessionRejectsExpiredToken() {
        AppUser user = userWithId(UUID.randomUUID());
        UserSession session = sessionWithExpiry(user, Instant.now().minusSeconds(1));
        when(sessionRepository.findBySessionToken("expired")).thenReturn(Optional.of(session));

        assertThat(userService.validateSession("expired")).isEmpty();
    }

    @Test
    void validateSessionReturnsEmptyForUnknownToken() {
        when(sessionRepository.findBySessionToken("unknown")).thenReturn(Optional.empty());

        assertThat(userService.validateSession("unknown")).isEmpty();
    }

    @Test
    void logoutDeletesSession() {
        AppUser user = userWithId(UUID.randomUUID());
        UserSession session = sessionWithExpiry(user, Instant.now().plusSeconds(3600));
        when(sessionRepository.findBySessionToken("tok")).thenReturn(Optional.of(session));

        userService.logout("tok");

        verify(sessionRepository).delete(session);
    }

    @Test
    void logoutDoesNothingForUnknownToken() {
        when(sessionRepository.findBySessionToken("nope")).thenReturn(Optional.empty());

        userService.logout("nope");

        verify(sessionRepository, never()).delete(any());
    }

    private static AppUser userWithId(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setPasswordHash("hash");
        return user;
    }

    private static UserSession sessionWithExpiry(AppUser user, Instant expiresAt) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken("token-" + UUID.randomUUID());
        session.setExpiresAt(expiresAt);
        return session;
    }
}
