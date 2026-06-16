package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

public record FinishChallengeRequest(UUID teamId, UUID enemyTeamId, Integer callShot) {

}
