package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games/{gameId}/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse createStation(@PathVariable UUID gameId, @Valid @RequestBody CreateStationRequest request) {
        return stationService.createStation(gameId, request);
    }

    @PostMapping("{stationId}/chips")
    @ResponseStatus(HttpStatus.OK)
    public StationChipStateResponse createTeam(@PathVariable UUID gameId, @PathVariable UUID stationId, @Valid @RequestBody AddChipsRequest request) {
        return stationService.addChipsToStation(gameId, stationId, request);
    }
}
