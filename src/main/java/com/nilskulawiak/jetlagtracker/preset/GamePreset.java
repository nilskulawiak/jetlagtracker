package com.nilskulawiak.jetlagtracker.preset;

import java.util.List;

public record GamePreset(
        String id,
        String name,
        String mapImage,
        int mapWidth,
        int mapHeight,
        List<StationPreset> stations,
        List<ChallengePreset> challenges
) {
}
