package com.smartgarden.backend.reading;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Atualizacoes em tempo real do SmartGarden")
public class ReadingEventStreamController {

    private final ReadingEventStreamService eventStreamService;

    public ReadingEventStreamController(ReadingEventStreamService eventStreamService) {
        this.eventStreamService = eventStreamService;
    }

    @GetMapping(path = "/readings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Acompanhar leituras", description = "Abre um fluxo SSE, opcionalmente filtrado por dispositivo.")
    public SseEmitter readings(@RequestParam(required = false) String deviceCode) {
        return eventStreamService.subscribe(deviceCode);
    }
}
