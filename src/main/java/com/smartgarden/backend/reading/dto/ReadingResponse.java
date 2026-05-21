package com.smartgarden.backend.reading.dto;

import com.smartgarden.backend.reading.EnvironmentalReading;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReadingResponse(
        Long id,
        Long deviceId,
        String deviceCode,
        String deviceName,
        BigDecimal temperatureC,
        BigDecimal humidityPercent,
        OffsetDateTime recordedAt,
        OffsetDateTime receivedAt
) {

    public static ReadingResponse fromEntity(EnvironmentalReading entity) {
        return new ReadingResponse(
                entity.getId(),
                entity.getDevice().getId(),
                entity.getDevice().getDeviceCode(),
                entity.getDevice().getName(),
                entity.getTemperatureC(),
                entity.getHumidityPercent(),
                entity.getRecordedAt(),
                entity.getReceivedAt()
        );
    }
}

