package com.smartgarden.backend.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Ponto agregado da serie temporal do relatorio")
public record ReportChartPointResponse(
        OffsetDateTime recordedAt,
        BigDecimal averageTemperatureC,
        BigDecimal averageHumidityPercent,
        long readings
) {
}
