package com.smartgarden.backend.dashboard.dto;

import com.smartgarden.backend.reading.dto.ReadingResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Ultima leitura conhecida de um dispositivo")
public record DeviceLatestReadingSummaryResponse(
        @Schema(example = "1")
        Long deviceId,
        @Schema(example = "esp32-jardim-bloco-a")
        String deviceCode,
        @Schema(example = "ESP32 Jardim Bloco A")
        String deviceName,
        @Schema(example = "Universidade - Bloco A")
        String location,
        @Schema(example = "true")
        boolean active,
        @Schema(example = "2026-05-21T12:16:53Z")
        OffsetDateTime lastSeenAt,
        ReadingResponse latestReading
) {
}

