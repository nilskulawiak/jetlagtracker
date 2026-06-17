package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import com.nilskulawiak.jetlagtracker.game.Game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Challenge {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Game game;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer xCoordinate;

    @Column(nullable = false)
    private Integer yCoordinate;

    @Column(nullable = false)
    private int reward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status = ChallengeStatus.CREATED;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private ChallengeType challengeType;

    @Version
    private int version;
}
