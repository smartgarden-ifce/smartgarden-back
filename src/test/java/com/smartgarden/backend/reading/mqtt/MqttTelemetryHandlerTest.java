package com.smartgarden.backend.reading.mqtt;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartgarden.backend.reading.EnvironmentalReadingService;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MqttTelemetryHandlerTest {

    private EnvironmentalReadingService readingService;
    private MqttTelemetryHandler handler;

    @BeforeEach
    void setUp() {
        readingService = mock(EnvironmentalReadingService.class);
        handler = new MqttTelemetryHandler(
                JsonMapper.builder().addModule(new JavaTimeModule()).build(),
                Validation.buildDefaultValidatorFactory().getValidator(),
                readingService
        );
    }

    @Test
    void shouldConvertValidTelemetryIntoAReading() {
        String payload = """
                {"messageId":"9d892fe8-d62a-4bd9-a0cc-a4c70f78271e","temperatureC":27.4,"humidityPercent":63.1,"recordedAt":null}
                """;

        handler.handle(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "smartgarden/devices/esp32-jardim/telemetry")
                .build());

        ArgumentCaptor<CreateReadingRequest> captor = ArgumentCaptor.forClass(CreateReadingRequest.class);
        verify(readingService).createReading(captor.capture());
        assertThat(captor.getValue().deviceCode()).isEqualTo("esp32-jardim");
        assertThat(captor.getValue().temperatureC()).isEqualByComparingTo("27.4");
        assertThat(captor.getValue().recordedAt()).isNull();
    }

    @Test
    void shouldDiscardInvalidTopic() {
        handler.handle(MessageBuilder.withPayload("{}")
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "smartgarden/invalid")
                .build());

        verifyNoInteractions(readingService);
    }

    @Test
    void shouldDiscardValuesOutsideSensorLimits() {
        String payload = """
                {"messageId":"9d892fe8-d62a-4bd9-a0cc-a4c70f78271e","temperatureC":90,"humidityPercent":63.1}
                """;

        handler.handle(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.RECEIVED_TOPIC, "smartgarden/devices/esp32-jardim/telemetry")
                .build());

        verifyNoInteractions(readingService);
    }
}
