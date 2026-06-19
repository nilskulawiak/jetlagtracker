package com.nilskulawiak.jetlagtracker.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findBySessionToken(String sessionToken);

    @Transactional
    void deleteByUser(AppUser user);
}
