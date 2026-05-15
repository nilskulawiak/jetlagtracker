package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationChipStateRepository extends JpaRepository<StationChipState, UUID>{

}

