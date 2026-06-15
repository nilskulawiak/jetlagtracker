package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

public record ChallengeAttemptResponse(UUID teamId, ChallengeAttemptStatus status) {

    public static ChallengeAttemptResponse from(ChallengeAttempt attempt) {
        return new ChallengeAttemptResponse(
                attempt.getTeam().getId(),
                attempt.getStatus());
    }
}
