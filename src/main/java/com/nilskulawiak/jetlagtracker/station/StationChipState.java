package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import com.nilskulawiak.jetlagtracker.team.Team;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"station_id", "team_id"}
    )
)
public class StationChipState {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Station station;

    @ManyToOne
    private Team team;

    private int chips;

    @Version
    private int version;
}