package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StartChallengeRequest(@NotNull UUID teamId) {
}
