package com.smartgarden.backend.report;

import com.smartgarden.backend.common.ApiErrorResponse;
import com.smartgarden.backend.report.dto.EnvironmentalReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/reports")
@Validated
@Tag(name = "Reports", description = "Relatorios ambientais calculados sob demanda")
public class EnvironmentalReportController {

    private final EnvironmentalReportService reportService;

    public EnvironmentalReportController(EnvironmentalReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/environmental")
    @Operation(
            summary = "Gerar relatorio ambiental",
            description = "Calcula indicadores, serie temporal e excecoes para um dispositivo em um periodo de ate 31 dias."
    )
    @ApiResponse(responseCode = "200", description = "Relatorio gerado")
    @ApiResponse(responseCode = "400", description = "Periodo invalido", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Dispositivo inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public EnvironmentalReportResponse getEnvironmentalReport(
            @RequestParam String deviceCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endAt
    ) {
        return reportService.generate(deviceCode, startAt, endAt);
    }
}
