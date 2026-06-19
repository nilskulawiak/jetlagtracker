package com.nilskulawiak.jetlagtracker.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.common.exception.ForbiddenException;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.user.AppUser;

@ExtendWith(MockitoExtension.class)
class MembershipAuthHelperTest {

    @Mock private GameMembershipRepository membershipRepository;
    @Mock private GameRepository gameRepository;

    @InjectMocks private MembershipAuthHelper authHelper;

    @Test
    void requireMemberReturnsMembership() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.PLAYER);
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThat(authHelper.requireMember(user, gameId)).isSameAs(membership);
    }

    @Test
    void requireMemberThrowsWhenNotMember() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authHelper.requireMember(user, gameId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Not a member of this game");
    }

    @Test
    void requireHostReturnsMembershipForHost() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.HOST);
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThat(authHelper.requireHost(user, gameId)).isSameAs(membership);
    }

    @Test
    void requireHostThrowsForPlayer() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.PLAYER);
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> authHelper.requireHost(user, gameId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Host access required");
    }

    @Test
    void requireTeamManagementAllowsAnyMemberDuringCreated() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.PLAYER);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameWithStatus(gameId, GameStatus.CREATED)));
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThat(authHelper.requireTeamManagement(user, gameId)).isSameAs(membership);
    }

    @Test
    void requireTeamManagementAllowsHostAfterCreated() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.HOST);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameWithStatus(gameId, GameStatus.STARTED)));
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThat(authHelper.requireTeamManagement(user, gameId)).isSameAs(membership);
    }

    @Test
    void requireTeamManagementThrowsForPlayerAfterCreated() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        GameMembership membership = membershipWithRole(user, gameId, MemberRole.PLAYER);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameWithStatus(gameId, GameStatus.STARTED)));
        when(membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> authHelper.requireTeamManagement(user, gameId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Team management is only open to all members during game setup");
    }

    @Test
    void requireTeamManagementThrowsWhenGameNotFound() {
        AppUser user = userWithId(UUID.randomUUID());
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authHelper.requireTeamManagement(user, gameId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found");
    }

    private static AppUser userWithId(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setPasswordHash("hash");
        return user;
    }

    private static Game gameWithStatus(UUID id, GameStatus status) {
        Game game = new Game();
        game.setId(id);
        game.setName("Game");
        game.setStatus(status);
        return game;
    }

    private static GameMembership membershipWithRole(AppUser user, UUID gameId, MemberRole role) {
        Game game = new Game();
        game.setId(gameId);
        GameMembership m = new GameMembership();
        m.setUser(user);
        m.setGame(game);
        m.setRole(role);
        return m;
    }
}
