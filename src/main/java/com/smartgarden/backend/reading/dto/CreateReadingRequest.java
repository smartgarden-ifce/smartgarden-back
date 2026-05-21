package com.smartgarden.backend.reading.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateReadingRequest(
        @NotBlank
        @Size(max = 100)
        String deviceCode,

        @NotNull
        @DecimalMin("-40.00")
        @DecimalMax("80.00")
        BigDecimal temperatureC,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal humidityPercent,

        OffsetDateTime recordedAt
) {
}

