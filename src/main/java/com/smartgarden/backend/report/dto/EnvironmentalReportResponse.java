package com.smartgarden.backend.report.dto;

import com.smartgarden.backend.environment.dto.EnvironmentalCriteriaResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Relatorio ambiental calculado sob demanda para um dispositivo")
public record EnvironmentalReportResponse(
        OffsetDateTime generatedAt,
        Long deviceId,
        String deviceCode,
        String deviceName,
        String location,
        OffsetDateTime windowStart,
        OffsetDateTime windowEnd,
        EnvironmentalCriteriaResponse criteria,
        ReportSummaryResponse summary,
        List<ReportChartPointResponse> chartPoints,
        long totalExceptions,
        boolean exceptionsTruncated,
        List<ReportExceptionResponse> exceptions
) {
}
