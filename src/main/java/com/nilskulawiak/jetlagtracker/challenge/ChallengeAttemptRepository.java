package com.nilskulawiak.jetlagtracker.challenge;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.team.Team;

public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, UUID> {

    Optional<ChallengeAttempt> findByChallengeAndTeam(Challenge challenge, Team team);

    long countByChallengeAndStatus(Challenge challenge, ChallengeAttemptStatus status);

    void deleteByChallenge(Challenge challenge);

    List<ChallengeAttempt> findByChallengeIn(Collection<Challenge> challenge);
}
