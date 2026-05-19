package com.nilskulawiak.jetlagtracker.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.action.GameAction;
import com.nilskulawiak.jetlagtracker.action.GameActionRepository;
import com.nilskulawiak.jetlagtracker.action.GameActionResponse;
import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.challenge.Challenge;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeResponse;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStatus;
import com.nilskulawiak.jetlagtracker.preset.ChallengePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePresetService;
import com.nilskulawiak.jetlagtracker.preset.StationPreset;
import com.nilskulawiak.jetlagtracker.station.Station;
import com.nilskulawiak.jetlagtracker.station.StationChipState;
import com.nilskulawiak.jetlagtracker.station.StationChipStateRepository;
import com.nilskulawiak.jetlagtracker.station.StationChipStateResponse;
import com.nilskulawiak.jetlagtracker.station.StationRepository;
import com.nilskulawiak.jetlagtracker.station.StationStateResponse;
import com.nilskulawiak.jetlagtracker.team.CreateTeamRequest;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;
import com.nilskulawiak.jetlagtracker.team.TeamResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final ChallengeRepository challengeRepository;
    private final StationRepository stationRepository;
    private final TeamRepository teamRepository;
    private final StationChipStateRepository stationChipStateRepository;
    private final GameActionService gameActionService;
    private final GameActionRepository gameActionRepository;
    private final GamePresetService gamePresetService;

    public GameResponse createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setName(request.name());
        game.setMapHeight(request.mapHeight());
        game.setMapWidth(request.mapWidth());
        game.setMapImage(request.mapImage());

        Game savedGame = gameRepository.save(game);

        gameActionService.log(
                savedGame,
                GameActionType.GAME_CREATED,
                savedGame.getName() + " was created"
        );

        return GameResponse.from(savedGame);
    }

    @Transactional
    public GameResponse startGame(UUID gameId, StartGameRequest request){
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        List<Challenge> createdChallenges = challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED);

        if (createdChallenges.size() < request.numberOfChallenges()) {
            throw new IllegalArgumentException(
                    "Not enough challenges"
            );
        }

        game.setStatus(GameStatus.STARTED);

        Collections.shuffle(createdChallenges);
        createdChallenges.stream().limit(request.numberOfChallenges()).forEach(challenge -> challenge.setStatus(ChallengeStatus.AVAILABLE));

        gameActionService.log(
                game,
                GameActionType.GAME_STARTED,
                game.getName() + " was started"
        );

        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse finishGame(UUID gameId){
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        game.setStatus(GameStatus.DONE);

        gameActionService.log(
                game,
                GameActionType.GAME_FINISHED,
                game.getName() + " was finished"
        );

        return GameResponse.from(game);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGameState(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        List<Team> teams = teamRepository.findByGame(game);
        List<Station> stations = stationRepository.findByGame(game);
        List<Challenge> challenges = challengeRepository.findByGame(game);
        List<GameAction> gameAction = gameActionRepository.findByGameOrderByCreatedAtDesc(game);

        List<StationStateResponse> stationResponses = stations.stream()
                .map(station -> {
                    List<StationChipState> chipStates =
                            stationChipStateRepository.findByStation(station);

                    UUID ownerTeamId = calculateStationOwner(station)
                            .map(Team::getId)
                            .orElse(null);

                    return new StationStateResponse(
                            station.getId(),
                            station.getName(),
                            station.getXCoordinate(),
                            station.getYCoordinate(),
                            ownerTeamId,
                            chipStates.stream()
                                    .map(StationChipStateResponse::from)
                                    .toList()
                    );
                })
                .toList();

        return new GameStateResponse(
                GameResponse.from(game),
                teams.stream().map(TeamResponse::from).toList(),
                stationResponses,
                challenges.stream().map(ChallengeResponse::from).toList(),
                gameAction.stream().map(GameActionResponse::from).toList()
        );
    }

    public GamesResponse getGames(){
        List<Game> games = gameRepository.findAll();
        return GamesResponse.from(games);
    }

@Transactional
public GameResponse createGameFromPreset(CreateGameFromPresetRequest request) {
    GamePreset preset = gamePresetService.loadPreset(request.presetId());

    Game game = new Game();
    game.setName(request.name());
    game.setMapImage(preset.mapImage());
    game.setMapWidth(preset.mapWidth());
    game.setMapHeight(preset.mapHeight());
    game.setStatus(GameStatus.CREATED);

    gameRepository.save(game);

    for (CreateTeamRequest teamRequest : request.teams()) {
        Team team = new Team();
        team.setGame(game);
        team.setName(teamRequest.name());
        team.setColor(teamRequest.color());
        team.setAvailableChips(teamRequest.startingChips());
        teamRepository.save(team);
    }

    for (StationPreset stationPreset : preset.stations()) {
        Station station = new Station();
        station.setGame(game);
        station.setName(stationPreset.name());
        station.setXCoordinate(stationPreset.xCoordinate());
        station.setYCoordinate(stationPreset.yCoordinate());
        stationRepository.save(station);
    }

    for (ChallengePreset challengePreset : preset.challenges()) {
        Challenge challenge = new Challenge();
        challenge.setGame(game);
        challenge.setName(challengePreset.name());
        challenge.setDescription(challengePreset.description());
        challenge.setReward(challengePreset.reward());
        challenge.setXCoordinate(challengePreset.xCoordinate());
        challenge.setYCoordinate(challengePreset.yCoordinate());
        challenge.setStatus(ChallengeStatus.CREATED);
        challenge.setChallengeType(challengePreset.challengeType());
        challengeRepository.save(challenge);
    }

    return GameResponse.from(game);
}

    private Optional<Team> calculateStationOwner(Station station) {
        return stationChipStateRepository.findByStation(station)
                .stream()
                .max(Comparator.comparingInt(StationChipState::getChips))
                .map(StationChipState::getTeam);
        }
}