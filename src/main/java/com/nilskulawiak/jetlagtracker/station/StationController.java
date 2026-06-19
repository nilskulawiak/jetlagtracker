package com.nilskulawiak.jetlagtracker.station;

import com.nilskulawiak.jetlagtracker.membership.MembershipAuthHelper;
import com.nilskulawiak.jetlagtracker.user.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/games/{gameId}/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final MembershipAuthHelper authHelper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse createStation(@AuthenticationPrincipal AppUser user,
                                         @PathVariable UUID gameId,
                                         @Valid @RequestBody CreateStationRequest request) {
        authHelper.requireHost(user, gameId);
        return stationService.createStation(gameId, request);
    }

    @PostMapping("/{stationId}/chips")
    @ResponseStatus(HttpStatus.OK)
    public StationChipStateResponse addChipsToStation(@AuthenticationPrincipal AppUser user,
                                                      @PathVariable UUID gameId,
                                                      @PathVariable UUID stationId,
                                                      @Valid @RequestBody AddChipsRequest request) {
        authHelper.requireMember(user, gameId);
        return stationService.addChipsToStation(gameId, stationId, request);
    }

    @DeleteMapping("/{stationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStation(@AuthenticationPrincipal AppUser user,
                              @PathVariable UUID gameId,
                              @PathVariable UUID stationId) {
        authHelper.requireHost(user, gameId);
        stationService.deleteStation(gameId, stationId);
    }

    @PatchMapping("/{stationId}")
    public StationResponse patchStation(@AuthenticationPrincipal AppUser user,
                                        @PathVariable UUID gameId,
                                        @PathVariable UUID stationId,
                                        @Valid @RequestBody PatchStationRequest request) {
        authHelper.requireHost(user, gameId);
        return stationService.patchStation(gameId, stationId, request);
    }

    @PutMapping("/{stationId}/chips/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public StationChipStateResponse setStationChips(@AuthenticationPrincipal AppUser user,
                                                    @PathVariable UUID gameId,
                                                    @PathVariable UUID stationId,
                                                    @PathVariable UUID teamId,
                                                    @Valid @RequestBody SetStationChipsRequest request) {
        authHelper.requireHost(user, gameId);
        return stationService.setStationChips(gameId, stationId, teamId, request);
    }
}
