package com.nilskulawiak.jetlagtracker.auth;

import com.nilskulawiak.jetlagtracker.user.AppUser;
import com.nilskulawiak.jetlagtracker.user.UserService;
import com.nilskulawiak.jetlagtracker.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        UserSession session = userService.register(request.email(), request.displayName(), request.password());
        return toAuthResponse(session);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        UserSession session = userService.login(request.email(), request.password());
        return toAuthResponse(session);
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal AppUser user,
                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        userService.logout(token);
    }

    @GetMapping("/me")
    public UserDTO me(@AuthenticationPrincipal AppUser user) {
        return toUserDTO(user);
    }

    private AuthResponse toAuthResponse(UserSession session) {
        return new AuthResponse(session.getSessionToken(), toUserDTO(session.getUser()));
    }

    private UserDTO toUserDTO(AppUser user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getDisplayName());
    }

}
