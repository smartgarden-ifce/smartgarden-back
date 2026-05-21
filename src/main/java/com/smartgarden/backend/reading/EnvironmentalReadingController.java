package com.smartgarden.backend.reading;

import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class EnvironmentalReadingController {

    private final EnvironmentalReadingService readingService;

    public EnvironmentalReadingController(EnvironmentalReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingResponse createReading(@Valid @RequestBody CreateReadingRequest request) {
        return readingService.createReading(request);
    }

    @GetMapping
    public List<ReadingResponse> listReadings(
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endAt
    ) {
        return readingService.listReadings(deviceCode, limit, startAt, endAt);
    }

    @GetMapping("/latest")
    public ReadingResponse getLatestReading(@RequestParam String deviceCode) {
        return readingService.getLatestReading(deviceCode);
    }
}

