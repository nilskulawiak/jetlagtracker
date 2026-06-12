package com.nilskulawiak.jetlagtracker.team;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameRepository;
import com.nilskulawiak.jetlagtracker.game.GameStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final GameActionService gameActionService;

    public TeamResponse createTeam(UUID gameId, CreateTeamRequest request) {
        Integer rawChips = request.startingChips();
        int startingChips = rawChips != null ? rawChips : 0;

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Teams can only be created before the game starts");
        }

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