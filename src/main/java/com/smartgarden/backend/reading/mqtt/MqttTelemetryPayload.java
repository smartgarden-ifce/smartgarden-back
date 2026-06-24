package com.smartgarden.backend.reading.mqtt;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MqttTelemetryPayload(
        @NotNull UUID messageId,
        @NotNull @DecimalMin("-40.00") @DecimalMax("80.00") BigDecimal temperatureC,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal humidityPercent,
        OffsetDateTime recordedAt
) {
}
