package com.nilskulawiak.jetlagtracker.preset;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/preset")
@RequiredArgsConstructor
public class GamePresetController {
    private final GamePresetService gamePresetService;


    @GetMapping
    public List<PresetSummaryResponse> getPresets() {
        return gamePresetService.getAvailablePresets();
    }

    @GetMapping("/{presetId}")
    public GamePreset getPreset(@PathVariable String presetId) {
        return gamePresetService.loadPreset(presetId);
    }
}
