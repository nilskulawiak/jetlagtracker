package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameInviteRepository extends JpaRepository<GameInvite, UUID> {
    Optional<GameInvite> findByInviteCode(String inviteCode);
    List<GameInvite> findByGame(Game game);
    boolean existsByInviteCode(String inviteCode);

    @Transactional
    void deleteByGame(Game game);
}
