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

    public void deleteTeam(UUID gameId, UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Teams can only be deleted before the game starts");
        }

        teamRepository.delete(team);
    }

    public TeamResponse patchTeam(UUID gameId, UUID teamId, PatchTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!team.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Team does not belong to this game");
        }

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Teams can only be updated before the game starts");
        }

        if (request.name() != null) team.setName(request.name());
        if (request.color() != null) team.setColor(request.color());
        if (request.availableChips() != null) team.setAvailableChips(request.availableChips());

        return TeamResponse.from(teamRepository.save(team));
    }
}