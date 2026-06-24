package com.smartgarden.backend.reading.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgarden.backend.reading.EnvironmentalReadingService;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttTelemetryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttTelemetryHandler.class);
    private static final Pattern TELEMETRY_TOPIC = Pattern.compile("^smartgarden/devices/([^/]{1,100})/telemetry$");

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final EnvironmentalReadingService readingService;

    public MqttTelemetryHandler(
            ObjectMapper objectMapper,
            Validator validator,
            EnvironmentalReadingService readingService
    ) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.readingService = readingService;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handle(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        String payload = message.getPayload() instanceof byte[] bytes
                ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                : message.getPayload().toString();

        try {
            String deviceCode = extractDeviceCode(topic);
            MqttTelemetryPayload telemetry = objectMapper.readValue(payload, MqttTelemetryPayload.class);
            validate(telemetry);
            readingService.createReading(new CreateReadingRequest(
                    deviceCode,
                    telemetry.temperatureC(),
                    telemetry.humidityPercent(),
                    telemetry.recordedAt(),
                    telemetry.messageId()
            ));
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            LOGGER.warn("Mensagem MQTT de telemetria descartada. topico={}, motivo={}", topic, exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.error("Falha ao processar telemetria MQTT. topico={}", topic, exception);
        }
    }

    private String extractDeviceCode(String topic) {
        if (topic == null) {
            throw new IllegalArgumentException("Topico MQTT ausente");
        }
        Matcher matcher = TELEMETRY_TOPIC.matcher(topic);
        if (!matcher.matches() || matcher.group(1).isBlank()) {
            throw new IllegalArgumentException("Topico MQTT invalido");
        }
        return matcher.group(1);
    }

    private void validate(MqttTelemetryPayload telemetry) {
        Set<ConstraintViolation<MqttTelemetryPayload>> violations = validator.validate(telemetry);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(details);
        }
    }
}
