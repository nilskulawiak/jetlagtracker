package com.nilskulawiak.jetlagtracker.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.game.GameStatus;

@ExtendWith(MockitoExtension.class)
class GameActionServiceTest {

    @Mock
    private GameActionRepository gameActionRepository;

    @InjectMocks
    private GameActionService gameActionService;

    @Test
    void logSavesActionWithCorrectFields() {
        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setName("Taiwan");
        game.setStatus(GameStatus.STARTED);
        game.setMapWidth(1000);
        game.setMapHeight(800);
        game.setMapImage("map.png");

        gameActionService.log(game, GameActionType.GAME_STARTED, "Taiwan was started");

        ArgumentCaptor<GameAction> captor = ArgumentCaptor.forClass(GameAction.class);
        verify(gameActionRepository).save(captor.capture());

        GameAction saved = captor.getValue();
        assertThat(saved.getGame()).isSameAs(game);
        assertThat(saved.getType()).isEqualTo(GameActionType.GAME_STARTED);
        assertThat(saved.getMessage()).isEqualTo("Taiwan was started");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
