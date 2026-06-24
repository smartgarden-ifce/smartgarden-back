package com.smartgarden.backend.reading.dto;

import com.smartgarden.backend.reading.EnvironmentalReading;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReadingResponse(
        @Schema(example = "2")
        Long id,
        @Schema(example = "1")
        Long deviceId,
        @Schema(example = "esp32-jardim-bloco-a")
        String deviceCode,
        @Schema(example = "ESP32 Jardim Bloco A")
        String deviceName,
        @Schema(example = "9d892fe8-d62a-4bd9-a0cc-a4c70f78271e", nullable = true)
        UUID messageId,
        @Schema(example = "31.90")
        BigDecimal temperatureC,
        @Schema(example = "64.60")
        BigDecimal humidityPercent,
        @Schema(example = "2026-05-21T12:16:53.231716916Z")
        OffsetDateTime recordedAt,
        @Schema(example = "2026-05-21T12:16:53.235524803Z")
        OffsetDateTime receivedAt,
        @Schema(example = "2026-05-21T12:16:53.235524803Z")
        OffsetDateTime createdAt,
        @Schema(example = "2026-05-21T12:16:53.235524803Z")
        OffsetDateTime updatedAt
) {

    public static ReadingResponse fromEntity(EnvironmentalReading entity) {
        return new ReadingResponse(
                entity.getId(),
                entity.getDevice().getId(),
                entity.getDevice().getDeviceCode(),
                entity.getDevice().getName(),
                entity.getMessageId(),
                entity.getTemperatureC(),
                entity.getHumidityPercent(),
                entity.getRecordedAt(),
                entity.getReceivedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
