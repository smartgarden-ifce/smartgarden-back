package com.smartgarden.backend.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateReadingRequest(
        @Schema(description = "Codigo unico do dispositivo ESP32", example = "esp32-jardim-bloco-a")
        @NotBlank
        @Size(max = 100)
        String deviceCode,

        @Schema(description = "Temperatura em graus Celsius", example = "27.40")
        @NotNull
        @DecimalMin("-40.00")
        @DecimalMax("80.00")
        BigDecimal temperatureC,

        @Schema(description = "Umidade relativa do ar em percentual", example = "63.10")
        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal humidityPercent,

        @Schema(description = "Momento em que a leitura foi capturada no dispositivo. Se omitido, o backend usa o horario atual.", example = "2026-05-21T14:30:00Z")
        OffsetDateTime recordedAt
) {
}
