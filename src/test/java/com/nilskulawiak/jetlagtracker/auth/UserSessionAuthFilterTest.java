package com.nilskulawiak.jetlagtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nilskulawiak.jetlagtracker.user.AppUser;
import com.nilskulawiak.jetlagtracker.user.UserService;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class UserSessionAuthFilterTest {

    @Mock private UserService userService;
    @InjectMocks private UserSessionAuthFilter filter;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenSetsAuthenticationInSecurityContext() throws Exception {
        AppUser user = userWithId(UUID.randomUUID());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        FilterChain chain = mock(FilterChain.class);

        when(userService.validateSession("valid-token")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void invalidTokenDoesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        FilterChain chain = mock(FilterChain.class);

        when(userService.validateSession("bad-token")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(any(), any());
    }

    @Test
    void missingHeaderSkipsValidationAndProceedsChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userService, never()).validateSession(any());
        verify(chain).doFilter(any(), any());
    }

    private static AppUser userWithId(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setPasswordHash("hash");
        return user;
    }
}
