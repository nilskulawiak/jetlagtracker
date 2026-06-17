package com.nilskulawiak.jetlagtracker.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nilskulawiak.jetlagtracker.action.GameAction;
import com.nilskulawiak.jetlagtracker.action.GameActionRepository;
import com.nilskulawiak.jetlagtracker.action.GameActionResponse;
import com.nilskulawiak.jetlagtracker.action.GameActionService;
import com.nilskulawiak.jetlagtracker.action.GameActionType;
import com.nilskulawiak.jetlagtracker.challenge.Challenge;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeAttempt;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeAttemptRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeRepository;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStateResponse;
import com.nilskulawiak.jetlagtracker.challenge.ChallengeStatus;
import com.nilskulawiak.jetlagtracker.preset.ChallengePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePreset;
import com.nilskulawiak.jetlagtracker.preset.GamePresetService;
import com.nilskulawiak.jetlagtracker.preset.StationPreset;
import com.nilskulawiak.jetlagtracker.station.Station;
import com.nilskulawiak.jetlagtracker.station.StationChipState;
import com.nilskulawiak.jetlagtracker.station.StationChipStateRepository;
import com.nilskulawiak.jetlagtracker.station.StationRepository;
import com.nilskulawiak.jetlagtracker.station.StationStateResponse;
import com.nilskulawiak.jetlagtracker.team.CreateTeamRequest;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.team.TeamRepository;
import com.nilskulawiak.jetlagtracker.team.TeamResponse;

import com.nilskulawiak.jetlagtracker.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
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
            .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Game cannot be started in status: " + game.getStatus());
        }

        List<Challenge> createdChallenges = challengeRepository.findByGameAndStatus(game, ChallengeStatus.CREATED);

        if (createdChallenges.size() < request.numberOfChallenges()) {
            throw new IllegalArgumentException(
                    "Not enough challenges"
            );
        }

        game.setStatus(GameStatus.STARTED);

        List<Challenge> shuffled = new ArrayList<>(createdChallenges);
        Collections.shuffle(shuffled);
        shuffled.stream().limit(request.numberOfChallenges()).forEach(challenge -> challenge.setStatus(ChallengeStatus.AVAILABLE));

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
            .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalArgumentException("Game cannot be finished in status: " + game.getStatus());
        }

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
                .orElseThrow(() -> new NotFoundException("Game not found"));

        List<Team> teams = teamRepository.findByGame(game);
        List<Station> stations = stationRepository.findByGame(game);
        List<Challenge> challenges = challengeRepository.findByGame(game);
        List<GameAction> gameAction = gameActionRepository.findByGameOrderByCreatedAtDesc(game);

        Map<UUID, List<StationChipState>> chipStatesByStation =
                stationChipStateRepository.findByStationIn(stations).stream()
                        .collect(Collectors.groupingBy(cs -> cs.getStation().getId()));

        List<StationStateResponse> stationResponses = stations.stream()
                .map(station -> StationStateResponse.from(
                        station,
                        chipStatesByStation.getOrDefault(station.getId(), List.of())))
                .toList();

        Map<UUID, List<ChallengeAttempt>> challengeAttemptsByChallenge =
                challengeAttemptRepository.findByChallengeIn(challenges).stream()
                        .collect(Collectors.groupingBy(cs -> cs.getChallenge().getId()));

        List<ChallengeStateResponse> challengeResponses = challenges.stream()
                .map(challenge -> ChallengeStateResponse.from(
                        challenge,
                        challengeAttemptsByChallenge.getOrDefault(challenge.getId(), List.of())))
                .toList();

        return new GameStateResponse(
                GameResponse.from(game),
                teams.stream().map(TeamResponse::from).toList(),
                stationResponses,
                challengeResponses,
                gameAction.stream().map(GameActionResponse::from).toList()
        );
    }

    @Transactional
    public void deleteGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        gameActionRepository.deleteByGame(game);
        challengeRepository.findByGame(game).forEach(challengeAttemptRepository::deleteByChallenge);
        challengeRepository.deleteByGame(game);
        stationRepository.findByGame(game).forEach(stationChipStateRepository::deleteByStation);
        stationRepository.deleteByGame(game);
        teamRepository.deleteByGame(game);
        gameRepository.delete(game);
    }

    @Transactional
    public GameResponse patchGame(UUID gameId, PatchGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.CREATED) {
            throw new IllegalArgumentException("Games can only be updated before they start");
        }

        if (request.name() != null) game.setName(request.name());
        if (request.mapWidth() != null) game.setMapWidth(request.mapWidth());
        if (request.mapHeight() != null) game.setMapHeight(request.mapHeight());
        if (request.mapImage() != null) game.setMapImage(request.mapImage());

        return GameResponse.from(game);
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

        gameActionService.log(
                game,
                GameActionType.GAME_CREATED,
                game.getName() + " was created"
        );

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
}