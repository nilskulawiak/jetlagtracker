package com.nilskulawiak.jetlagtracker.game;

import jakarta.validation.constraints.Positive;

public record StartGameRequest(@Positive int numberOfChallenges) {

}
