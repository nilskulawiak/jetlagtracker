package com.nilskulawiak.jetlagtracker.team;

import com.nilskulawiak.jetlagtracker.membership.MembershipAuthHelper;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/games/{gameId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final MembershipAuthHelper authHelper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@AuthenticationPrincipal AppUser user,
                                   @PathVariable UUID gameId,
                                   @Valid @RequestBody CreateTeamRequest request) {
        authHelper.requireTeamManagement(user, gameId);
        return teamService.createTeam(gameId, request);
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@AuthenticationPrincipal AppUser user,
                           @PathVariable UUID gameId,
                           @PathVariable UUID teamId) {
        authHelper.requireTeamManagement(user, gameId);
        teamService.deleteTeam(gameId, teamId);
    }

    @PatchMapping("/{teamId}")
    public TeamResponse patchTeam(@AuthenticationPrincipal AppUser user,
                                  @PathVariable UUID gameId,
                                  @PathVariable UUID teamId,
                                  @Valid @RequestBody PatchTeamRequest request) {
        authHelper.requireTeamManagement(user, gameId);
        return teamService.patchTeam(gameId, teamId, request);
    }
}
