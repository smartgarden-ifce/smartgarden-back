package com.smartgarden.backend.reading;

import com.smartgarden.backend.reading.dto.ReadingResponse;

public record ReadingCreatedEvent(ReadingResponse reading) {
}
