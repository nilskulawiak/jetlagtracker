package com.nilskulawiak.jetlagtracker.team;

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
@RequestMapping("/games/{gameId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@PathVariable UUID gameId, @Valid @RequestBody CreateTeamRequest request) {
        return teamService.createTeam(gameId, request);
    }
}
