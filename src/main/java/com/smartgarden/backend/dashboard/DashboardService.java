package com.smartgarden.backend.dashboard;

import com.smartgarden.backend.dashboard.dto.DashboardSummaryResponse;
import com.smartgarden.backend.dashboard.dto.DashboardAlertResponse;
import com.smartgarden.backend.dashboard.dto.DeviceLatestReadingSummaryResponse;
import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.environment.EnvironmentalCriteriaService;
import com.smartgarden.backend.reading.EnvironmentalReading;
import com.smartgarden.backend.reading.EnvironmentalReadingRepository;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final long OFFLINE_MINUTES_THRESHOLD = 15;

    private final DeviceService deviceService;
    private final EnvironmentalReadingRepository readingRepository;
    private final EnvironmentalCriteriaService criteriaService;

    public DashboardService(
            DeviceService deviceService,
            EnvironmentalReadingRepository readingRepository,
            EnvironmentalCriteriaService criteriaService
    ) {
        this.deviceService = deviceService;
        this.readingRepository = readingRepository;
        this.criteriaService = criteriaService;
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

        List<DashboardAlertResponse> alerts = buildAlerts(generatedAt, latestReadings);

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
                alerts.size(),
                alerts,
                latestReadings,
                criteriaService.getCriteria()
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

    private List<DashboardAlertResponse> buildAlerts(
            OffsetDateTime generatedAt,
            List<DeviceLatestReadingSummaryResponse> latestReadings
    ) {
        List<DashboardAlertResponse> alerts = new ArrayList<>();

        for (DeviceLatestReadingSummaryResponse device : latestReadings) {
            if (device.lastSeenAt() == null || device.lastSeenAt().isBefore(generatedAt.minusMinutes(OFFLINE_MINUTES_THRESHOLD))) {
                alerts.add(new DashboardAlertResponse(
                        "SENSOR_OFFLINE",
                        "critical",
                        "Sensor sem comunicação recente",
                        "%s está sem enviar leituras há mais de %d minutos.".formatted(device.deviceName(), OFFLINE_MINUTES_THRESHOLD),
                        device.deviceCode(),
                        device.deviceName()
                ));
            }

            if (device.latestReading() == null) {
                continue;
            }

            BigDecimal temperature = device.latestReading().temperatureC();
            BigDecimal humidity = device.latestReading().humidityPercent();

            if (temperature.compareTo(EnvironmentalCriteriaService.TEMPERATURE_MAX_C) > 0) {
                alerts.add(new DashboardAlertResponse(
                        "TEMPERATURE_HIGH",
                        "warning",
                        "Temperatura acima da faixa",
                        "%s registrou %s °C.".formatted(device.deviceName(), temperature),
                        device.deviceCode(),
                        device.deviceName()
                ));
            } else if (temperature.compareTo(EnvironmentalCriteriaService.TEMPERATURE_MIN_C) < 0) {
                alerts.add(new DashboardAlertResponse(
                        "TEMPERATURE_LOW",
                        "info",
                        "Temperatura abaixo da faixa",
                        "%s registrou %s °C.".formatted(device.deviceName(), temperature),
                        device.deviceCode(),
                        device.deviceName()
                ));
            }

            if (humidity.compareTo(EnvironmentalCriteriaService.HUMIDITY_MIN_PERCENT) < 0) {
                alerts.add(new DashboardAlertResponse(
                        "HUMIDITY_LOW",
                        "warning",
                        "Umidade abaixo da faixa",
                        "%s registrou %s%% de umidade.".formatted(device.deviceName(), humidity),
                        device.deviceCode(),
                        device.deviceName()
                ));
            } else if (humidity.compareTo(EnvironmentalCriteriaService.HUMIDITY_MAX_PERCENT) > 0) {
                alerts.add(new DashboardAlertResponse(
                        "HUMIDITY_HIGH",
                        "warning",
                        "Umidade acima da faixa",
                        "%s registrou %s%% de umidade.".formatted(device.deviceName(), humidity),
                        device.deviceCode(),
                        device.deviceName()
                ));
            }
        }

        return alerts.stream()
                .sorted(Comparator.comparingInt(alert -> switch (alert.severity()) {
                    case "critical" -> 0;
                    case "warning" -> 1;
                    default -> 2;
                }))
                .toList();
    }
}
