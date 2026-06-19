package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.GameService;
import com.nilskulawiak.jetlagtracker.game.GameStateResponse;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final MembershipAuthHelper authHelper;
    private final GameService gameService;

    @GetMapping("/games/mine")
    public List<GameMembershipResponse> myGames(@AuthenticationPrincipal AppUser user) {
        return membershipService.getMyMemberships(user);
    }

    @PostMapping("/games/{gameId}/invites")
    public GameInviteResponse generateInvite(@AuthenticationPrincipal AppUser user,
                                             @PathVariable UUID gameId) {
        authHelper.requireHost(user, gameId);
        GameInvite invite = membershipService.generateInvite(user, gameId);
        return new GameInviteResponse(invite.getInviteCode());
    }

    @PatchMapping("/games/{gameId}/my-team")
    public void setMyTeam(@AuthenticationPrincipal AppUser user,
                          @PathVariable UUID gameId,
                          @RequestBody SetMyTeamRequest request) {
        authHelper.requireMember(user, gameId);
        membershipService.setMyTeam(user, gameId, request.teamId());
    }

    @PostMapping("/games/join")
    public JoinGameResponse joinGame(@AuthenticationPrincipal AppUser user,
                                     @RequestBody JoinGameRequest request) {
        GameMembership membership = membershipService.redeemInvite(user, request.inviteCode());
        GameStateResponse state = gameService.getGameState(membership.getGame().getId());
        return new JoinGameResponse(
            membership.getGame().getId(),
            membership.getRole(),
            membership.getTeam() != null ? membership.getTeam().getId() : null,
            state
        );
    }

}
