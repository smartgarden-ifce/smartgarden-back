package com.smartgarden.backend.dashboard;

import com.smartgarden.backend.dashboard.dto.DashboardSummaryResponse;
import com.smartgarden.backend.dashboard.dto.DeviceLatestReadingSummaryResponse;
import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.reading.EnvironmentalReading;
import com.smartgarden.backend.reading.EnvironmentalReadingRepository;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DeviceService deviceService;
    private final EnvironmentalReadingRepository readingRepository;

    public DashboardService(DeviceService deviceService, EnvironmentalReadingRepository readingRepository) {
        this.deviceService = deviceService;
        this.readingRepository = readingRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(int hours) {
        OffsetDateTime generatedAt = OffsetDateTime.now();
        OffsetDateTime windowStart = generatedAt.minusHours(hours);

        Map<Long, EnvironmentalReading> latestByDeviceId = readingRepository.findLatestReadingPerDevice().stream()
                .collect(Collectors.toMap(reading -> reading.getDevice().getId(), Function.identity()));

        var latestReadings = deviceService.listDeviceEntities().stream()
                .map(device -> toLatestReadingSummary(device, latestByDeviceId.get(device.getId())))
                .toList();

        return new DashboardSummaryResponse(
                generatedAt,
                windowStart,
                hours,
                deviceService.countDevices(),
                deviceService.countActiveDevices(),
                readingRepository.count(),
                readingRepository.countByRecordedAtGreaterThanEqual(windowStart),
                round(readingRepository.averageTemperatureSince(windowStart)),
                round(readingRepository.averageHumiditySince(windowStart)),
                latestReadings
        );
    }

    private DeviceLatestReadingSummaryResponse toLatestReadingSummary(Device device, EnvironmentalReading reading) {
        return new DeviceLatestReadingSummaryResponse(
                device.getId(),
                device.getDeviceCode(),
                device.getName(),
                device.getLocation(),
                device.isActive(),
                device.getLastSeenAt(),
                reading == null ? null : ReadingResponse.fromEntity(reading)
        );
    }

    private BigDecimal round(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}

