package com.nilskulawiak.jetlagtracker.challenge;

import java.time.Instant;
import java.util.UUID;

import com.nilskulawiak.jetlagtracker.team.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"challenge_id", "team_id"}
    )
)
@Getter
@Setter
public class ChallengeAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Challenge challenge;

    @ManyToOne(optional = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeAttemptStatus status;

    private Integer callShot;

    @Column(nullable = false)
    private Instant attemptedAt = Instant.now();
}