package com.nilskulawiak.jetlagtracker.preset;

import com.nilskulawiak.jetlagtracker.challenge.ChallengeType;

public record ChallengePreset(
        String name,
        String description,
        int reward,
        Integer xCoordinate,
        Integer yCoordinate,
        ChallengeType challengeType
) {
}
