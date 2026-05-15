package com.nilskulawiak.jetlagtracker.challenge;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.team.Team;

public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, UUID> {

    boolean existsByChallengeAndTeam(Challenge challenge, Team team);

    long countByChallengeAndSuccessFalse(Challenge challenge);
}
