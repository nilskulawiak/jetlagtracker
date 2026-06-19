package com.nilskulawiak.jetlagtracker.game;

import com.nilskulawiak.jetlagtracker.membership.MembershipAuthHelper;
import com.nilskulawiak.jetlagtracker.membership.MembershipService;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final MembershipService membershipService;
    private final MembershipAuthHelper authHelper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public GameResponse createGame(@AuthenticationPrincipal AppUser user,
                                   @Valid @RequestBody CreateGameRequest request) {
        GameResponse game = gameService.createGame(request);
        membershipService.createHostMembership(user, game.id());
        return game;
    }

    @PostMapping("/from-preset")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public GameResponse createGameFromPreset(@AuthenticationPrincipal AppUser user,
                                             @Valid @RequestBody CreateGameFromPresetRequest request) {
        GameResponse game = gameService.createGameFromPreset(request);
        membershipService.createHostMembership(user, game.id());
        return game;
    }

    @PostMapping("/{gameId}/start")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse startGame(@AuthenticationPrincipal AppUser user,
                                  @PathVariable UUID gameId,
                                  @Valid @RequestBody StartGameRequest request) {
        authHelper.requireHost(user, gameId);
        return gameService.startGame(gameId, request);
    }

    @PostMapping("/{gameId}/finish")
    @ResponseStatus(HttpStatus.OK)
    public GameResponse finishGame(@AuthenticationPrincipal AppUser user,
                                   @PathVariable UUID gameId) {
        authHelper.requireHost(user, gameId);
        return gameService.finishGame(gameId);
    }

    @GetMapping("/{gameId}/state")
    public GameStateResponse getGameState(@AuthenticationPrincipal AppUser user,
                                          @PathVariable UUID gameId) {
        authHelper.requireMember(user, gameId);
        return gameService.getGameState(gameId);
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@AuthenticationPrincipal AppUser user,
                           @PathVariable UUID gameId) {
        authHelper.requireHost(user, gameId);
        gameService.deleteGame(gameId);
    }

    @PatchMapping("/{gameId}")
    public GameResponse patchGame(@AuthenticationPrincipal AppUser user,
                                  @PathVariable UUID gameId,
                                  @Valid @RequestBody PatchGameRequest request) {
        authHelper.requireHost(user, gameId);
        return gameService.patchGame(gameId, request);
    }
}
