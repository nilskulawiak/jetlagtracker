package com.nilskulawiak.jetlagtracker.membership;

import com.nilskulawiak.jetlagtracker.game.Game;
import com.nilskulawiak.jetlagtracker.team.Team;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
public class GameMembership {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @ManyToOne
    private Team team;
}
