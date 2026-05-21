package com.smartgarden.backend.reading;

import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EnvironmentalReadingService {

    private final EnvironmentalReadingRepository readingRepository;
    private final DeviceService deviceService;

    public EnvironmentalReadingService(EnvironmentalReadingRepository readingRepository, DeviceService deviceService) {
        this.readingRepository = readingRepository;
        this.deviceService = deviceService;
    }

    @Transactional
    public ReadingResponse createReading(CreateReadingRequest request) {
        Device device = deviceService.findOrCreateByCode(request.deviceCode());

        EnvironmentalReading reading = new EnvironmentalReading();
        reading.setDevice(device);
        reading.setTemperatureC(request.temperatureC());
        reading.setHumidityPercent(request.humidityPercent());
        reading.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : OffsetDateTime.now());

        EnvironmentalReading saved = readingRepository.save(reading);
        return ReadingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> listReadings(
            String deviceCode,
            int limit,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        int sanitizedLimit = Math.min(Math.max(limit, 1), 500);
        PageRequest pageRequest = PageRequest.of(0, sanitizedLimit);

        List<EnvironmentalReading> readings;
        if (startAt != null && endAt != null && deviceCode != null && !deviceCode.isBlank()) {
            readings = readingRepository.findByDeviceDeviceCodeAndRecordedAtBetweenOrderByRecordedAtDesc(
                    deviceCode,
                    startAt,
                    endAt,
                    pageRequest
            );
        } else if (startAt != null && endAt != null) {
            readings = readingRepository.findByRecordedAtBetweenOrderByRecordedAtDesc(startAt, endAt, pageRequest);
        } else if (deviceCode != null && !deviceCode.isBlank()) {
            readings = readingRepository.findByDeviceDeviceCodeOrderByRecordedAtDesc(deviceCode, pageRequest);
        } else {
            readings = readingRepository.findAllByOrderByRecordedAtDesc(pageRequest);
        }

        return readings.stream()
                .map(ReadingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReadingResponse getLatestReading(String deviceCode) {
        EnvironmentalReading reading = readingRepository.findFirstByDeviceDeviceCodeOrderByRecordedAtDesc(deviceCode)
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma leitura encontrada para o dispositivo informado."));
        return ReadingResponse.fromEntity(reading);
    }
}

