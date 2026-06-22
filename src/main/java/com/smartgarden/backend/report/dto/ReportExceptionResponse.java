package com.smartgarden.backend.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Leitura fora de pelo menos uma faixa ambiental")
public record ReportExceptionResponse(
        Long readingId,
        OffsetDateTime recordedAt,
        BigDecimal temperatureC,
        BigDecimal humidityPercent,
        String temperatureStatus,
        String humidityStatus
) {
}
