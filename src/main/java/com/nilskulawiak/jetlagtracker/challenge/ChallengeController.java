package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games/{gameId}/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(@PathVariable UUID gameId, @Valid @RequestBody CreateChallengeRequest request) {
        return challengeService.createChallenge(gameId, request);
    }
}
