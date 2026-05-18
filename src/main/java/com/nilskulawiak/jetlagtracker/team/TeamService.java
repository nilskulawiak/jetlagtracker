package com.nilskulawiak.jetlagtracker.team;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final GameActionService gameActionService;

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

        gameActionService.log(
                game,
                GameActionType.TEAM_CREATED,
                team.getName() + " was created"
        );

        return TeamResponse.from(savedTeam);
    }
}