package com.nilskulawiak.jetlagtracker.challenge;

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
@RequestMapping("/games/{gameId}/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MembershipAuthHelper authHelper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(@AuthenticationPrincipal AppUser user,
                                             @PathVariable UUID gameId,
                                             @Valid @RequestBody CreateChallengeRequest request) {
        authHelper.requireHost(user, gameId);
        return challengeService.createChallenge(gameId, request);
    }

    @DeleteMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@AuthenticationPrincipal AppUser user,
                                @PathVariable UUID gameId,
                                @PathVariable UUID challengeId) {
        authHelper.requireHost(user, gameId);
        challengeService.deleteChallenge(gameId, challengeId);
    }

    @PatchMapping("/{challengeId}")
    public ChallengeResponse patchChallenge(@AuthenticationPrincipal AppUser user,
                                            @PathVariable UUID gameId,
                                            @PathVariable UUID challengeId,
                                            @Valid @RequestBody PatchChallengeRequest request) {
        authHelper.requireHost(user, gameId);
        return challengeService.patchChallenge(gameId, challengeId, request);
    }

    @PostMapping("/{challengeId}/start")
    @ResponseStatus(HttpStatus.OK)
    public ChallengeResponse startChallenge(@AuthenticationPrincipal AppUser user,
                                            @PathVariable UUID gameId,
                                            @PathVariable UUID challengeId,
                                            @Valid @RequestBody StartChallengeRequest request) {
        authHelper.requireMember(user, gameId);
        return challengeService.startChallenge(gameId, challengeId, request);
    }

    @PostMapping("/{challengeId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public ChallengeResponse completeChallenge(@AuthenticationPrincipal AppUser user,
                                               @PathVariable UUID gameId,
                                               @PathVariable UUID challengeId,
                                               @Valid @RequestBody FinishChallengeRequest request) {
        authHelper.requireMember(user, gameId);
        return challengeService.completeChallenge(gameId, challengeId, request);
    }

    @PostMapping("/{challengeId}/fail")
    @ResponseStatus(HttpStatus.OK)
    public ChallengeResponse failChallenge(@AuthenticationPrincipal AppUser user,
                                           @PathVariable UUID gameId,
                                           @PathVariable UUID challengeId,
                                           @Valid @RequestBody FinishChallengeRequest request) {
        authHelper.requireMember(user, gameId);
        return challengeService.failChallenge(gameId, challengeId, request);
    }

    @PostMapping("/{challengeId}/revert-to-created")
    @ResponseStatus(HttpStatus.OK)
    public ChallengeResponse revertChallengeToCreated(@AuthenticationPrincipal AppUser user,
                                                      @PathVariable UUID gameId,
                                                      @PathVariable UUID challengeId) {
        authHelper.requireHost(user, gameId);
        return challengeService.revertChallengeToCreated(gameId, challengeId);
    }

    @DeleteMapping("/{challengeId}/attempts/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallengeAttempt(@AuthenticationPrincipal AppUser user,
                                       @PathVariable UUID gameId,
                                       @PathVariable UUID challengeId,
                                       @PathVariable UUID teamId) {
        authHelper.requireHost(user, gameId);
        challengeService.deleteChallengeAttempt(gameId, challengeId, teamId);
    }
}
