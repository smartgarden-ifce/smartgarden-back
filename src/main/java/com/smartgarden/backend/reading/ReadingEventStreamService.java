package com.smartgarden.backend.reading;

import com.smartgarden.backend.reading.dto.ReadingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Service
public class ReadingEventStreamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadingEventStreamService.class);

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Client> clients = new ConcurrentHashMap<>();
    private final Supplier<SseEmitter> emitterFactory;

    public ReadingEventStreamService() {
        this(() -> new SseEmitter(0L));
    }

    ReadingEventStreamService(Supplier<SseEmitter> emitterFactory) {
        this.emitterFactory = emitterFactory;
    }

    public SseEmitter subscribe(String deviceCode) {
        long clientId = sequence.incrementAndGet();
        SseEmitter emitter = emitterFactory.get();
        clients.put(clientId, new Client(emitter, normalize(deviceCode)));

        Runnable removeClient = () -> clients.remove(clientId);
        emitter.onCompletion(removeClient);
        emitter.onTimeout(removeClient);
        emitter.onError(error -> removeClient.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data(OffsetDateTime.now().toString()));
        } catch (IOException exception) {
            clients.remove(clientId);
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReadingCreated(ReadingCreatedEvent event) {
        ReadingResponse reading = event.reading();
        clients.forEach((clientId, client) -> {
            if (client.deviceCode() == null || client.deviceCode().equals(reading.deviceCode())) {
                send(clientId, client, SseEmitter.event()
                        .id(reading.id().toString())
                        .name("reading-created")
                        .data(reading));
            }
        });
    }

    @Scheduled(fixedRateString = "${app.sse.heartbeat-interval-ms:15000}")
    public void sendHeartbeat() {
        String timestamp = OffsetDateTime.now().toString();
        clients.forEach((clientId, client) -> send(
                clientId,
                client,
                SseEmitter.event().name("heartbeat").data(timestamp)
        ));
    }

    private void send(Long clientId, Client client, SseEmitter.SseEventBuilder event) {
        try {
            client.emitter().send(event);
        } catch (IOException | IllegalStateException exception) {
            clients.remove(clientId);
            client.emitter().complete();
            LOGGER.debug("Cliente SSE removido apos falha de envio", exception);
        }
    }

    private String normalize(String deviceCode) {
        return deviceCode == null || deviceCode.isBlank() ? null : deviceCode.trim();
    }

    private record Client(SseEmitter emitter, String deviceCode) {
    }
}
