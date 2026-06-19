package com.nilskulawiak.jetlagtracker.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import com.nilskulawiak.jetlagtracker.user.AppUserRepository;
import com.nilskulawiak.jetlagtracker.user.UserService;
import com.nilskulawiak.jetlagtracker.user.UserSession;

@SpringBootTest
@Testcontainers
@Transactional
class MembershipIntegrationTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired private UserService userService;
    @Autowired private MembershipService membershipService;
    @Autowired private GameRepository gameRepository;
    @Autowired private AppUserRepository userRepository;

    @Test
    void registerPersistsUserAndSession() {
        UserSession session = userService.register("new@example.com", "New User", "password123");

        assertThat(session.getSessionToken()).isNotNull();
        assertThat(session.getUser().getEmail()).isEqualTo("new@example.com");
        assertThat(userRepository.existsByEmail("new@example.com")).isTrue();
    }

    @Test
    void duplicateEmailIsRejected() {
        userService.register("dup@example.com", "First", "password");

        assertThatThrownBy(() -> userService.register("dup@example.com", "Second", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void validateSessionReturnsCorrectUser() {
        UserSession session = userService.register("val@example.com", "Val", "password");

        Optional<AppUser> result = userService.validateSession(session.getSessionToken());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("val@example.com");
    }

    @Test
    void validateSessionReturnsEmptyForUnknownToken() {
        assertThat(userService.validateSession("does-not-exist")).isEmpty();
    }

    @Test
    void fullMembershipFlow() {
        UserSession hostSession = userService.register("host@example.com", "Host", "password");
        AppUser host = hostSession.getUser();

        Game game = gameRepository.save(newGame());

        GameMembership hostMembership = membershipService.createHostMembership(host, game);
        assertThat(hostMembership.getRole()).isEqualTo(MemberRole.HOST);
        assertThat(hostMembership.getUser().getId()).isEqualTo(host.getId());

        GameInvite invite = membershipService.generateInvite(host, game.getId());
        assertThat(invite.getInviteCode()).hasSize(8);

        UserSession playerSession = userService.register("player@example.com", "Player", "password");
        AppUser player = playerSession.getUser();

        GameMembership playerMembership = membershipService.redeemInvite(player, invite.getInviteCode());
        assertThat(playerMembership.getRole()).isEqualTo(MemberRole.PLAYER);
        assertThat(playerMembership.getGame().getId()).isEqualTo(game.getId());
    }

    @Test
    void redeemInviteIsIdempotentForSamePlayer() {
        UserSession hostSession = userService.register("host2@example.com", "Host", "password");
        Game game = gameRepository.save(newGame());
        GameInvite invite = membershipService.generateInvite(hostSession.getUser(), game.getId());

        UserSession playerSession = userService.register("player2@example.com", "Player", "password");
        AppUser player = playerSession.getUser();

        GameMembership first = membershipService.redeemInvite(player, invite.getInviteCode());
        GameMembership second = membershipService.redeemInvite(player, invite.getInviteCode());

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    private static Game newGame() {
        Game game = new Game();
        game.setName("Integration Test Game");
        game.setMapWidth(800);
        game.setMapHeight(600);
        game.setMapImage("map.png");
        game.setStatus(GameStatus.CREATED);
        return game;
    }
}
