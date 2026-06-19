package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class GameInvite {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Game game;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private AppUser createdBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
