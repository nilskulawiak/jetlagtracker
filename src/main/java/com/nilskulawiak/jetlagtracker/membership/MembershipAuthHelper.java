package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.common.exception.ForbiddenException;
import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MembershipAuthHelper {

    private final GameMembershipRepository membershipRepository;
    private final GameRepository gameRepository;

    public GameMembership requireMember(AppUser user, UUID gameId) {
        return membershipRepository.findByUser_IdAndGame_Id(user.getId(), gameId)
            .orElseThrow(() -> new ForbiddenException("Not a member of this game"));
    }

    public GameMembership requireHost(AppUser user, UUID gameId) {
        GameMembership membership = requireMember(user, gameId);
        if (membership.getRole() != MemberRole.HOST) {
            throw new ForbiddenException("Host access required");
        }
        return membership;
    }

    public GameMembership requireTeamManagement(AppUser user, UUID gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new NotFoundException("Game not found"));
        GameMembership membership = requireMember(user, gameId);
        if (game.getStatus() != GameStatus.CREATED && membership.getRole() != MemberRole.HOST) {
            throw new ForbiddenException("Team management is only open to all members during game setup");
        }
        return membership;
    }
}
