package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameMembershipRepository extends JpaRepository<GameMembership, UUID> {
    Optional<GameMembership> findByUserAndGame(AppUser user, Game game);
    Optional<GameMembership> findByUser_IdAndGame_Id(UUID userId, UUID gameId);
    List<GameMembership> findByUser(AppUser user);

    @Transactional
    void deleteByGame(Game game);
}
