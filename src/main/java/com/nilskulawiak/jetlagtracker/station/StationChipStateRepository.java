package com.nilskulawiak.jetlagtracker.station;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nilskulawiak.jetlagtracker.team.Team;

public interface StationChipStateRepository extends JpaRepository<StationChipState, UUID>{
    Optional<StationChipState> findByStationAndTeam(Station station, Team team);

    List<StationChipState> findByStation(Station station);

    void deleteByStation(Station station);
}
