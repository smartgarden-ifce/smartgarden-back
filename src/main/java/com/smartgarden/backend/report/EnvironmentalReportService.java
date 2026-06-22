package com.smartgarden.backend.report;

import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.environment.EnvironmentalCriteriaService;
import com.smartgarden.backend.reading.EnvironmentalReading;
import com.smartgarden.backend.reading.EnvironmentalReadingRepository;
import com.smartgarden.backend.report.dto.EnvironmentalReportResponse;
import com.smartgarden.backend.report.dto.ReportChartPointResponse;
import com.smartgarden.backend.report.dto.ReportExceptionResponse;
import com.smartgarden.backend.report.dto.ReportSummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnvironmentalReportService {

    private static final int MAX_PERIOD_DAYS = 31;
    private static final int MAX_CHART_POINTS = 120;
    private static final int MAX_EXCEPTIONS = 50;

    private final EnvironmentalReadingRepository readingRepository;
    private final DeviceService deviceService;
    private final EnvironmentalCriteriaService criteriaService;

    public EnvironmentalReportService(
            EnvironmentalReadingRepository readingRepository,
            DeviceService deviceService,
            EnvironmentalCriteriaService criteriaService
    ) {
        this.readingRepository = readingRepository;
        this.deviceService = deviceService;
        this.criteriaService = criteriaService;
    }

    @Transactional(readOnly = true)
    public EnvironmentalReportResponse generate(String deviceCode, OffsetDateTime startAt, OffsetDateTime endAt) {
        validateRequest(deviceCode, startAt, endAt);

        Device device = deviceService.findByCode(deviceCode)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo não encontrado."));

        Specification<EnvironmentalReading> specification = Specification.allOf(
                belongsToDevice(deviceCode),
                recordedBetween(startAt, endAt)
        );
        List<EnvironmentalReading> readings = readingRepository.findAll(
                specification,
                Sort.by(Sort.Direction.ASC, "recordedAt")
        );

        ReportSummaryResponse summary = buildSummary(readings);
        List<ReportChartPointResponse> chartPoints = buildChartPoints(readings, startAt, endAt);
        List<EnvironmentalReading> exceptions = readings.stream()
                .filter(reading -> !criteriaService.isEnvironmentAdequate(
                        reading.getTemperatureC(), reading.getHumidityPercent()))
                .toList();
        List<ReportExceptionResponse> recentExceptions = exceptions.stream()
                .skip(Math.max(0, exceptions.size() - MAX_EXCEPTIONS))
                .map(this::toExceptionResponse)
                .toList()
                .reversed();

        return new EnvironmentalReportResponse(
                OffsetDateTime.now(),
                device.getId(),
                device.getDeviceCode(),
                device.getName(),
                device.getLocation(),
                startAt,
                endAt,
                criteriaService.getCriteria(),
                summary,
                chartPoints,
                exceptions.size(),
                exceptions.size() > MAX_EXCEPTIONS,
                recentExceptions
        );
    }

    private void validateRequest(String deviceCode, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (deviceCode == null || deviceCode.isBlank()) {
            throw new IllegalArgumentException("Informe o dispositivo do relatório.");
        }
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("Informe o início e o fim do período.");
        }
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("A data inicial deve ser anterior à data final.");
        }
        if (Duration.between(startAt.toInstant(), endAt.toInstant()).compareTo(Duration.ofDays(MAX_PERIOD_DAYS)) > 0) {
            throw new IllegalArgumentException("O período máximo do relatório é de 31 dias.");
        }
    }

    private ReportSummaryResponse buildSummary(List<EnvironmentalReading> readings) {
        if (readings.isEmpty()) {
            return new ReportSummaryResponse(0, null, null, null, null, null, null, 0, null);
        }

        BigDecimal temperatureSum = BigDecimal.ZERO;
        BigDecimal humiditySum = BigDecimal.ZERO;
        BigDecimal minimumTemperature = readings.getFirst().getTemperatureC();
        BigDecimal maximumTemperature = minimumTemperature;
        BigDecimal minimumHumidity = readings.getFirst().getHumidityPercent();
        BigDecimal maximumHumidity = minimumHumidity;
        long adequateReadings = 0;

        for (EnvironmentalReading reading : readings) {
            BigDecimal temperature = reading.getTemperatureC();
            BigDecimal humidity = reading.getHumidityPercent();
            temperatureSum = temperatureSum.add(temperature);
            humiditySum = humiditySum.add(humidity);
            minimumTemperature = minimumTemperature.min(temperature);
            maximumTemperature = maximumTemperature.max(temperature);
            minimumHumidity = minimumHumidity.min(humidity);
            maximumHumidity = maximumHumidity.max(humidity);
            if (criteriaService.isEnvironmentAdequate(temperature, humidity)) {
                adequateReadings++;
            }
        }

        BigDecimal count = BigDecimal.valueOf(readings.size());
        return new ReportSummaryResponse(
                readings.size(),
                temperatureSum.divide(count, 2, RoundingMode.HALF_UP),
                minimumTemperature,
                maximumTemperature,
                humiditySum.divide(count, 2, RoundingMode.HALF_UP),
                minimumHumidity,
                maximumHumidity,
                adequateReadings,
                BigDecimal.valueOf(adequateReadings)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(count, 2, RoundingMode.HALF_UP)
        );
    }

    private List<ReportChartPointResponse> buildChartPoints(
            List<EnvironmentalReading> readings,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (readings.isEmpty()) {
            return List.of();
        }

        long periodSeconds = Math.max(1, Duration.between(startAt.toInstant(), endAt.toInstant()).getSeconds());
        long bucketSeconds = Math.max(1, (long) Math.ceil(periodSeconds / (double) MAX_CHART_POINTS));
        Map<Long, ChartBucket> buckets = new LinkedHashMap<>();

        for (EnvironmentalReading reading : readings) {
            long elapsedSeconds = Math.max(0,
                    Duration.between(startAt.toInstant(), reading.getRecordedAt().toInstant()).getSeconds());
            long bucketIndex = Math.min(MAX_CHART_POINTS - 1, elapsedSeconds / bucketSeconds);
            buckets.computeIfAbsent(bucketIndex, ignored -> new ChartBucket())
                    .add(reading.getTemperatureC(), reading.getHumidityPercent());
        }

        List<ReportChartPointResponse> points = new ArrayList<>(buckets.size());
        buckets.forEach((index, bucket) -> points.add(new ReportChartPointResponse(
                startAt.plusSeconds(index * bucketSeconds),
                bucket.averageTemperature(),
                bucket.averageHumidity(),
                bucket.count
        )));
        return points;
    }

    private ReportExceptionResponse toExceptionResponse(EnvironmentalReading reading) {
        return new ReportExceptionResponse(
                reading.getId(),
                reading.getRecordedAt(),
                reading.getTemperatureC(),
                reading.getHumidityPercent(),
                criteriaService.temperatureStatus(reading.getTemperatureC()),
                criteriaService.humidityStatus(reading.getHumidityPercent())
        );
    }

    private Specification<EnvironmentalReading> belongsToDevice(String deviceCode) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("device").get("deviceCode"), deviceCode);
    }

    private Specification<EnvironmentalReading> recordedBetween(OffsetDateTime startAt, OffsetDateTime endAt) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(root.get("recordedAt"), startAt),
                criteriaBuilder.lessThanOrEqualTo(root.get("recordedAt"), endAt)
        );
    }

    private static final class ChartBucket {
        private BigDecimal temperatureSum = BigDecimal.ZERO;
        private BigDecimal humiditySum = BigDecimal.ZERO;
        private long count;

        void add(BigDecimal temperature, BigDecimal humidity) {
            temperatureSum = temperatureSum.add(temperature);
            humiditySum = humiditySum.add(humidity);
            count++;
        }

        BigDecimal averageTemperature() {
            return temperatureSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        BigDecimal averageHumidity() {
            return humiditySum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }
    }
}
