package com.nilskulawiak.jetlagtracker.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock private AppUserRepository userRepository;
    @InjectMocks private AppUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsUserForKnownEmail() {
        AppUser user = userWithEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThat(userDetailsService.loadUserByUsername("user@example.com")).isSameAs(user);
    }

    @Test
    void loadUserByUsernameNormalizesEmail() {
        AppUser user = userWithEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        userDetailsService.loadUserByUsername("  USER@EXAMPLE.COM  ");

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void loadUserByUsernameThrowsForUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private static AppUser userWithEmail(String email) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setDisplayName("User");
        user.setPasswordHash("hash");
        return user;
    }
}
