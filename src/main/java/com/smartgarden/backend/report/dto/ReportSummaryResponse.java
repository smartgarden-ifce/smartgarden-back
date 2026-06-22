package com.smartgarden.backend.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Indicadores agregados do relatorio ambiental")
public record ReportSummaryResponse(
        long totalReadings,
        BigDecimal averageTemperatureC,
        BigDecimal minimumTemperatureC,
        BigDecimal maximumTemperatureC,
        BigDecimal averageHumidityPercent,
        BigDecimal minimumHumidityPercent,
        BigDecimal maximumHumidityPercent,
        long adequateReadings,
        BigDecimal adequatePercentage
) {
}
