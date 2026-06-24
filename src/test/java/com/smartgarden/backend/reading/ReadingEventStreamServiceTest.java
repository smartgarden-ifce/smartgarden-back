package com.smartgarden.backend.reading;

import com.smartgarden.backend.reading.dto.ReadingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingEventStreamServiceTest {

    @Test
    void shouldBroadcastOnlyToGlobalAndMatchingSubscribers() {
        RecordingEmitter global = new RecordingEmitter();
        RecordingEmitter matching = new RecordingEmitter();
        RecordingEmitter other = new RecordingEmitter();
        ArrayDeque<RecordingEmitter> emitters = new ArrayDeque<>(List.of(global, matching, other));
        ReadingEventStreamService service = new ReadingEventStreamService(emitters::removeFirst);

        service.subscribe(null);
        service.subscribe("esp32-01");
        service.subscribe("esp32-02");
        ReadingResponse reading = reading("esp32-01");

        service.onReadingCreated(new ReadingCreatedEvent(reading));

        assertThat(global.payloads).contains(reading);
        assertThat(matching.payloads).contains(reading);
        assertThat(other.payloads).doesNotContain(reading);
    }

    @Test
    void shouldSendHeartbeatToConnectedClients() {
        RecordingEmitter emitter = new RecordingEmitter();
        ReadingEventStreamService service = new ReadingEventStreamService(() -> emitter);
        service.subscribe(null);
        int payloadsAfterConnection = emitter.payloads.size();

        service.sendHeartbeat();

        assertThat(emitter.payloads).hasSizeGreaterThan(payloadsAfterConnection);
    }

    private ReadingResponse reading(String deviceCode) {
        OffsetDateTime now = OffsetDateTime.now();
        return new ReadingResponse(
                1L, 1L, deviceCode, "Sensor", null,
                new BigDecimal("25.00"), new BigDecimal("60.00"),
                now, now, now, now
        );
    }

    private static class RecordingEmitter extends SseEmitter {
        private final List<Object> payloads = new ArrayList<>();

        @Override
        public synchronized void send(SseEventBuilder event) throws IOException {
            event.build().stream()
                    .map(DataWithMediaType::getData)
                    .forEach(payloads::add);
        }
    }
}
