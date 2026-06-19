package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.common.exception.ForbiddenException;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no I, O, 0, 1
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GameMembershipRepository membershipRepository;
    private final GameInviteRepository inviteRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public GameMembership createHostMembership(AppUser user, Game game) {
        GameMembership membership = new GameMembership();
        membership.setUser(user);
        membership.setGame(game);
        membership.setRole(MemberRole.HOST);
        return membershipRepository.save(membership);
    }

    @Transactional
    public GameMembership redeemInvite(AppUser user, String inviteCode) {
        GameInvite invite = inviteRepository.findByInviteCode(inviteCode.toUpperCase().strip())
            .orElseThrow(() -> new NotFoundException("Invite code not found"));

        Optional<GameMembership> existing = membershipRepository.findByUserAndGame(user, invite.getGame());
        if (existing.isPresent()) {
            return existing.get();
        }

        GameMembership membership = new GameMembership();
        membership.setUser(user);
        membership.setGame(invite.getGame());
        membership.setRole(MemberRole.PLAYER);
        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public Optional<GameMembership> getMembership(AppUser user, Game game) {
        return membershipRepository.findByUserAndGame(user, game);
    }

    @Transactional
    public void setMyTeam(AppUser user, UUID gameId, UUID teamId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new NotFoundException("Game not found"));
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new NotFoundException("Team not found"));
        GameMembership membership = membershipRepository.findByUserAndGame(user, game)
            .orElseThrow(() -> new ForbiddenException("Not a member of this game"));
        membership.setTeam(team);
    }

    @Transactional
    public GameInvite generateInvite(AppUser host, UUID gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new NotFoundException("Game not found"));
        GameInvite invite = new GameInvite();
        invite.setGame(game);
        invite.setCreatedBy(host);
        invite.setInviteCode(generateUniqueCode());
        return inviteRepository.save(invite);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = RANDOM.ints(CODE_LENGTH, 0, CODE_CHARS.length())
                .mapToObj(i -> String.valueOf(CODE_CHARS.charAt(i)))
                .collect(Collectors.joining());
        } while (inviteRepository.existsByInviteCode(code));
        return code;
    }
}
