package com.nilskulawiak.jetlagtracker.preset;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GamePresetService {

    private final ObjectMapper objectMapper;

    public List<PresetSummaryResponse> getAvailablePresets() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:presets/*/preset.json");

            return Arrays.stream(resources)
                    .map(this::loadPresetFromResource)
                    .map(preset -> new PresetSummaryResponse(
                            preset.id(),
                            preset.name(),
                            preset.mapImage()
                    ))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Could not scan presets", e);
        }
    }

    public GamePreset loadPreset(String presetId) {
        try {
            String path = "presets/" + presetId + "/preset.json";

            InputStream inputStream = new ClassPathResource(path).getInputStream();

            return objectMapper.readValue(inputStream, GamePreset.class);

        } catch (IOException e) {
            throw new RuntimeException("Could not load preset: " + presetId, e);
        }
    }

    private GamePreset loadPresetFromResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, GamePreset.class);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read preset file: " + resource.getFilename(),
                    e
            );
        }
    }

}