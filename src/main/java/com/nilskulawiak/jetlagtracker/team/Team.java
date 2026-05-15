package com.nilskulawiak.jetlagtracker.team;

import java.util.UUID;

import com.nilskulawiak.jetlagtracker.game.Game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Game game;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer availableChips;
}