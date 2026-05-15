package com.nilskulawiak.jetlagtracker.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status = "SETUP";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}