package com.smartgarden.backend.reading;

import com.smartgarden.backend.device.DeviceRepository;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
class EnvironmentalReadingIdempotencyTest {

    @Autowired
    private EnvironmentalReadingService readingService;

    @Autowired
    private EnvironmentalReadingRepository readingRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @BeforeEach
    void cleanDatabase() {
        readingRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    @Test
    void shouldPersistOnlyOneReadingForTheSameMessageId() {
        UUID messageId = UUID.randomUUID();
        CreateReadingRequest request = new CreateReadingRequest(
                "esp32-test",
                new BigDecimal("27.40"),
                new BigDecimal("63.10"),
                null,
                messageId
        );

        var first = readingService.createReading(request);
        var duplicate = readingService.createReading(request);

        assertThat(duplicate.id()).isEqualTo(first.id());
        assertThat(duplicate.messageId()).isEqualTo(messageId);
        assertThat(duplicate.recordedAt()).isNotNull();
        assertThat(readingRepository.count()).isOne();
        assertThat(deviceRepository.count()).isOne();
        assertThat(applicationEvents.stream(ReadingCreatedEvent.class)).hasSize(1);
    }
}
