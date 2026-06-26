package com.smartgarden.backend.environment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Criterios globais usados para interpretar as leituras ambientais")
public record EnvironmentalCriteriaResponse(
        @Schema(example = "20") BigDecimal temperatureMinC,
        @Schema(example = "30") BigDecimal temperatureMaxC,
        @Schema(example = "40") BigDecimal humidityMinPercent,
        @Schema(example = "70") BigDecimal humidityMaxPercent,
        @Schema(example = "Temperatura agradavel entre 20 e 30 °C.") String temperatureDescription,
        @Schema(example = "Umidade normal entre 40% e 70%.") String humidityDescription,
        @Schema(example = "O ambiente e adequado quando temperatura e umidade estao simultaneamente dentro das faixas.") String environmentDescription
) {
}
