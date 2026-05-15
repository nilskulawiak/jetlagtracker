package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import com.nilskulawiak.jetlagtracker.team.Team;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StationChipState {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Station station;

    @ManyToOne
    private Team team;

    private int chips;
}