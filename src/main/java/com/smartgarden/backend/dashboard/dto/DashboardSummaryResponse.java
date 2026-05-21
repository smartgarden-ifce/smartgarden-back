package com.smartgarden.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Resumo agregado para exibicao no dashboard")
public record DashboardSummaryResponse(
        @Schema(example = "2026-05-21T12:30:00Z")
        OffsetDateTime generatedAt,
        @Schema(example = "2026-05-20T12:30:00Z")
        OffsetDateTime windowStart,
        @Schema(example = "24")
        int windowHours,
        @Schema(example = "3")
        long totalDevices,
        @Schema(example = "3")
        long activeDevices,
        @Schema(example = "158")
        long totalReadings,
        @Schema(example = "48")
        long readingsInWindow,
        @Schema(example = "28.45")
        BigDecimal averageTemperatureCInWindow,
        @Schema(example = "62.17")
        BigDecimal averageHumidityPercentInWindow,
        List<DeviceLatestReadingSummaryResponse> latestReadingsByDevice
) {
}

