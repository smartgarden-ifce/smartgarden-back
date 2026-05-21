package com.smartgarden.backend.device.dto;

import com.smartgarden.backend.device.Device;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record DeviceResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "esp32-jardim-bloco-a")
        String deviceCode,
        @Schema(example = "ESP32 Jardim Bloco A")
        String name,
        @Schema(example = "Universidade - Bloco A")
        String location,
        @Schema(example = "true")
        boolean active,
        @Schema(example = "2026-05-21T12:00:00Z")
        OffsetDateTime createdAt,
        @Schema(example = "2026-05-21T12:16:53Z")
        OffsetDateTime updatedAt,
        @Schema(example = "2026-05-21T12:16:53Z")
        OffsetDateTime lastSeenAt
) {

    public static DeviceResponse fromEntity(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getDeviceCode(),
                device.getName(),
                device.getLocation(),
                device.isActive(),
                device.getCreatedAt(),
                device.getUpdatedAt(),
                device.getLastSeenAt()
        );
    }
}
