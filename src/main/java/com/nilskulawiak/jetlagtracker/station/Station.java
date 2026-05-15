package com.nilskulawiak.jetlagtracker.station;

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
public class Station {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Game game;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double xCoordinate;

    @Column(nullable = false)
    private Double yCoordinate;
}