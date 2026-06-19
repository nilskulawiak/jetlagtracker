package com.nilskulawiak.jetlagtracker.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;
import com.nilskulawiak.jetlagtracker.user.AppUser;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock private GameMembershipRepository membershipRepository;
    @Mock private GameInviteRepository inviteRepository;
    @Mock private GameRepository gameRepository;
    @Mock private TeamRepository teamRepository;

    @InjectMocks private MembershipService membershipService;

    @Test
    void createHostMembershipSavesHostRole() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GameMembership result = membershipService.createHostMembership(user, game);

        assertThat(result.getRole()).isEqualTo(MemberRole.HOST);
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getGame()).isSameAs(game);
    }

    @Test
    void redeemInviteCreatesPlayerMembership() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        GameInvite invite = inviteFor(game, "ABCD1234");

        when(inviteRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(invite));
        when(membershipRepository.findByUserAndGame(user, game)).thenReturn(Optional.empty());
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GameMembership result = membershipService.redeemInvite(user, "ABCD1234");

        assertThat(result.getRole()).isEqualTo(MemberRole.PLAYER);
        assertThat(result.getGame()).isSameAs(game);
    }

    @Test
    void redeemInviteNormalizesCode() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        GameInvite invite = inviteFor(game, "ABCD1234");

        when(inviteRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(invite));
        when(membershipRepository.findByUserAndGame(user, game)).thenReturn(Optional.empty());
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        membershipService.redeemInvite(user, " abcd1234 ");

        verify(inviteRepository).findByInviteCode("ABCD1234");
    }

    @Test
    void redeemInviteIsIdempotentForExistingMember() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        GameInvite invite = inviteFor(game, "ABCD1234");
        GameMembership existing = membershipWithRole(user, game, MemberRole.PLAYER);

        when(inviteRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(invite));
        when(membershipRepository.findByUserAndGame(user, game)).thenReturn(Optional.of(existing));

        GameMembership result = membershipService.redeemInvite(user, "ABCD1234");

        assertThat(result).isSameAs(existing);
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void redeemInviteThrowsForInvalidCode() {
        when(inviteRepository.findByInviteCode("BADCODE1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.redeemInvite(userWithId(UUID.randomUUID()), "BADCODE1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Invite code not found");
    }

    @Test
    void generateInviteSavesInviteWithCode() {
        AppUser host = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(inviteRepository.existsByInviteCode(any())).thenReturn(false);
        when(inviteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GameInvite result = membershipService.generateInvite(host, game.getId());

        assertThat(result.getInviteCode()).hasSize(8);
        assertThat(result.getGame()).isSameAs(game);
        assertThat(result.getCreatedBy()).isSameAs(host);
    }

    @Test
    void setMyTeamUpdatesMembership() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);
        GameMembership membership = membershipWithRole(user, game, MemberRole.PLAYER);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(membershipRepository.findByUserAndGame(user, game)).thenReturn(Optional.of(membership));

        membershipService.setMyTeam(user, game.getId(), team.getId());

        assertThat(membership.getTeam()).isSameAs(team);
    }

    @Test
    void setMyTeamThrowsWhenNotMember() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        Team team = teamWithId(UUID.randomUUID(), game);

        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(membershipRepository.findByUserAndGame(user, game)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.setMyTeam(user, game.getId(), team.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getMyMembershipsReturnsMappedList() {
        AppUser user = userWithId(UUID.randomUUID());
        Game game = gameWithId(UUID.randomUUID());
        GameMembership membership = membershipWithRole(user, game, MemberRole.HOST);

        when(membershipRepository.findByUser(user)).thenReturn(List.of(membership));

        List<GameMembershipResponse> result = membershipService.getMyMemberships(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).gameId()).isEqualTo(game.getId());
        assertThat(result.get(0).role()).isEqualTo(MemberRole.HOST);
        assertThat(result.get(0).teamId()).isNull();
        assertThat(result.get(0).teamName()).isNull();
    }

    private static AppUser userWithId(UUID id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setPasswordHash("hash");
        return user;
    }

    private static Game gameWithId(UUID id) {
        Game game = new Game();
        game.setId(id);
        game.setName("Test Game");
        game.setMapWidth(800);
        game.setMapHeight(600);
        game.setMapImage("map.png");
        game.setStatus(GameStatus.CREATED);
        return game;
    }

    private static Team teamWithId(UUID id, Game game) {
        Team team = new Team();
        team.setId(id);
        team.setGame(game);
        team.setName("Team A");
        team.setColor("#ff0000");
        team.setAvailableChips(30);
        return team;
    }

    private static GameInvite inviteFor(Game game, String code) {
        GameInvite invite = new GameInvite();
        invite.setGame(game);
        invite.setInviteCode(code);
        return invite;
    }

    private static GameMembership membershipWithRole(AppUser user, Game game, MemberRole role) {
        GameMembership m = new GameMembership();
        m.setUser(user);
        m.setGame(game);
        m.setRole(role);
        return m;
    }
}
