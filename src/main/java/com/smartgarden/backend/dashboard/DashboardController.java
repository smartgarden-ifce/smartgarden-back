package com.smartgarden.backend.dashboard;

import com.smartgarden.backend.dashboard.dto.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Validated
@Tag(name = "Dashboard", description = "Resumo agregado para o painel administrativo")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo do dashboard", description = "Retorna totais gerais, medias da janela informada e a ultima leitura de cada dispositivo.")
    public DashboardSummaryResponse getSummary(
            @RequestParam(defaultValue = "24")
            @Parameter(description = "Janela de horas usada nas metricas agregadas", example = "24")
            @Min(1) @Max(168) int hours
    ) {
        return dashboardService.getSummary(hours);
    }
}

