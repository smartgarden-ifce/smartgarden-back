package com.smartgarden.backend.device.dto;

import com.smartgarden.backend.device.Device;

import java.time.OffsetDateTime;

public record DeviceResponse(
        Long id,
        String deviceCode,
        String name,
        String location,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
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

