package com.smartgarden.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alerta ativo calculado a partir das leituras e do estado do sensor")
public record DashboardAlertResponse(
        @Schema(example = "TEMPERATURE_HIGH")
        String code,
        @Schema(example = "warning")
        String severity,
        @Schema(example = "Temperatura acima da faixa")
        String title,
        @Schema(example = "ESP32 Jardim Bloco A registrou 31.9 °C.")
        String message,
        @Schema(example = "esp32-jardim-bloco-a")
        String deviceCode,
        @Schema(example = "ESP32 Jardim Bloco A")
        String deviceName
) {
}
