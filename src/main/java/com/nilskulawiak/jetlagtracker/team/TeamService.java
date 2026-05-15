package com.nilskulawiak.jetlagtracker.team;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    public TeamResponse createTeam(UUID gameId, CreateTeamRequest request) {
        int startingChips = request.startingChips() != null
                ? request.startingChips()
                : 0;

        Game game = gameRepository.findById(gameId)
                .orElseThrow();

        Team team = new Team();
        team.setName(request.name());
        team.setColor(request.color());
        team.setAvailableChips(startingChips);
        team.setGame(game);

        Team savedTeam = teamRepository.save(team);

        return TeamResponse.from(savedTeam);
    }
}