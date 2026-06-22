package com.smartgarden.backend.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Serie historica de leituras para alimentar graficos no dashboard")
public record ReadingHistoryResponse(
        @Schema(example = "esp32-jardim-bloco-a")
        String deviceCode,
        @Schema(example = "ESP32 Jardim Bloco A")
        String deviceName,
        @Schema(example = "2026-05-27T18:00:00Z")
        OffsetDateTime windowStart,
        @Schema(example = "2026-05-27T20:00:00Z")
        OffsetDateTime windowEnd,
        @Schema(example = "120")
        int totalPoints,
        List<ReadingResponse> readings
) {
}
