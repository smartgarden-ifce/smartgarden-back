package com.smartgarden.backend.reading;

import com.smartgarden.backend.common.PageResponse;
import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import com.smartgarden.backend.reading.dto.ReadingHistoryResponse;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PageResponse<ReadingResponse> listReadings(
            String deviceCode,
            int page,
            int size,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.min(Math.max(size, 1), 200);
        PageRequest pageRequest = PageRequest.of(
                sanitizedPage,
                sanitizedSize,
                Sort.by(Sort.Direction.DESC, "recordedAt")
        );

        String normalizedDeviceCode = (deviceCode == null || deviceCode.isBlank()) ? null : deviceCode;
        Specification<EnvironmentalReading> specification = Specification.allOf(
                hasDeviceCode(normalizedDeviceCode),
                recordedAtGreaterThanOrEqualTo(startAt),
                recordedAtLessThanOrEqualTo(endAt)
        );

        Page<EnvironmentalReading> readings = readingRepository.findAll(specification, pageRequest);

        return PageResponse.from(readings.map(ReadingResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public ReadingResponse getLatestReading(String deviceCode) {
        EnvironmentalReading reading = readingRepository.findFirstByDeviceDeviceCodeOrderByRecordedAtDesc(deviceCode)
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma leitura encontrada para o dispositivo informado."));
        return ReadingResponse.fromEntity(reading);
    }

    @Transactional(readOnly = true)
    public ReadingHistoryResponse getReadingHistory(
            String deviceCode,
            int hours,
            int limit,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (deviceCode == null || deviceCode.isBlank()) {
            throw new EntityNotFoundException("Informe o deviceCode para consultar o histórico.");
        }

        OffsetDateTime windowEnd = endAt != null ? endAt : OffsetDateTime.now();
        OffsetDateTime windowStart = startAt != null ? startAt : windowEnd.minusHours(Math.max(hours, 1));
        int sanitizedLimit = Math.min(Math.max(limit, 2), 500);

        Specification<EnvironmentalReading> specification = Specification.allOf(
                hasDeviceCode(deviceCode),
                recordedAtGreaterThanOrEqualTo(windowStart),
                recordedAtLessThanOrEqualTo(windowEnd)
        );

        List<EnvironmentalReading> history = readingRepository.findAll(
                specification,
                Sort.by(Sort.Direction.ASC, "recordedAt")
        );

        List<ReadingResponse> points = history.stream()
                .map(ReadingResponse::fromEntity)
                .skip(Math.max(0, history.size() - sanitizedLimit))
                .toList();

        String deviceName = points.isEmpty()
                ? deviceService.findByCode(deviceCode).map(Device::getName).orElse(deviceCode)
                : points.get(0).deviceName();
        return new ReadingHistoryResponse(
                deviceCode,
                deviceName,
                windowStart,
                windowEnd,
                points.size(),
                points
        );
    }

    private Specification<EnvironmentalReading> hasDeviceCode(String deviceCode) {
        return (root, query, criteriaBuilder) -> {
            if (deviceCode == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("device").get("deviceCode"), deviceCode);
        };
    }

    private Specification<EnvironmentalReading> recordedAtGreaterThanOrEqualTo(OffsetDateTime startAt) {
        return (root, query, criteriaBuilder) -> {
            if (startAt == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("recordedAt"), startAt);
        };
    }

    private Specification<EnvironmentalReading> recordedAtLessThanOrEqualTo(OffsetDateTime endAt) {
        return (root, query, criteriaBuilder) -> {
            if (endAt == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("recordedAt"), endAt);
        };
    }
}
