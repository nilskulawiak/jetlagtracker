package com.nilskulawiak.jetlagtracker.action;

import java.time.Instant;
import java.util.UUID;

import com.nilskulawiak.jetlagtracker.game.Game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class GameAction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameActionType type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}