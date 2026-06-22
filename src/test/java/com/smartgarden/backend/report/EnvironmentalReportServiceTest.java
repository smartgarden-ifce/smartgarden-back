package com.smartgarden.backend.report;

import com.smartgarden.backend.device.Device;
import com.smartgarden.backend.device.DeviceService;
import com.smartgarden.backend.environment.EnvironmentalCriteriaService;
import com.smartgarden.backend.reading.EnvironmentalReading;
import com.smartgarden.backend.reading.EnvironmentalReadingRepository;
import com.smartgarden.backend.report.dto.EnvironmentalReportResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentalReportServiceTest {

    private static final OffsetDateTime START = OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime END = START.plusDays(7);

    @Mock
    private EnvironmentalReadingRepository readingRepository;
    @Mock
    private DeviceService deviceService;

    private EnvironmentalReportService service;
    private Device device;

    @BeforeEach
    void setUp() {
        service = new EnvironmentalReportService(
                readingRepository,
                deviceService,
                new EnvironmentalCriteriaService()
        );
        device = new Device();
        device.setDeviceCode("esp32-01");
        device.setName("Sensor Jardim");
        device.setLocation("Jardim principal");
    }

    @Test
    void shouldCalculateSummaryAndTreatBoundariesAsAdequate() {
        List<EnvironmentalReading> readings = List.of(
                reading("20", "40", START.plusHours(1)),
                reading("30", "70", START.plusHours(2)),
                reading("31", "50", START.plusHours(3))
        );
        mockReportData(readings);

        EnvironmentalReportResponse report = service.generate("esp32-01", START, END);

        assertThat(report.summary().totalReadings()).isEqualTo(3);
        assertThat(report.summary().averageTemperatureC()).isEqualByComparingTo("27.00");
        assertThat(report.summary().minimumTemperatureC()).isEqualByComparingTo("20");
        assertThat(report.summary().maximumTemperatureC()).isEqualByComparingTo("31");
        assertThat(report.summary().adequateReadings()).isEqualTo(2);
        assertThat(report.summary().adequatePercentage()).isEqualByComparingTo("66.67");
        assertThat(report.totalExceptions()).isEqualTo(1);
        assertThat(report.exceptions()).singleElement()
                .extracting(exception -> exception.temperatureStatus())
                .isEqualTo("Quente");
    }

    @Test
    void shouldReturnEmptyReportWhenThereAreNoReadings() {
        mockReportData(List.of());

        EnvironmentalReportResponse report = service.generate("esp32-01", START, END);

        assertThat(report.summary().totalReadings()).isZero();
        assertThat(report.summary().averageTemperatureC()).isNull();
        assertThat(report.summary().adequatePercentage()).isNull();
        assertThat(report.chartPoints()).isEmpty();
        assertThat(report.exceptions()).isEmpty();
    }

    @Test
    void shouldLimitChartAndExceptions() {
        List<EnvironmentalReading> readings = new ArrayList<>();
        for (int index = 0; index < 240; index++) {
            readings.add(reading("31", "71", START.plusMinutes(index * 30L)));
        }
        mockReportData(readings);

        EnvironmentalReportResponse report = service.generate("esp32-01", START, END);

        assertThat(report.chartPoints()).hasSizeLessThanOrEqualTo(120);
        assertThat(report.totalExceptions()).isEqualTo(240);
        assertThat(report.exceptions()).hasSize(50);
        assertThat(report.exceptionsTruncated()).isTrue();
        assertThat(report.exceptions().getFirst().recordedAt()).isEqualTo(readings.getLast().getRecordedAt());
    }

    @Test
    void shouldRejectInvalidPeriods() {
        assertThatThrownBy(() -> service.generate("esp32-01", END, START))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data inicial");
        assertThatThrownBy(() -> service.generate("esp32-01", START, START.plusDays(32)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("31 dias");
    }

    @Test
    void shouldRejectUnknownDevice() {
        when(deviceService.findByCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate("unknown", START, END))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }

    @SuppressWarnings("unchecked")
    private void mockReportData(List<EnvironmentalReading> readings) {
        when(deviceService.findByCode("esp32-01")).thenReturn(Optional.of(device));
        when(readingRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(readings);
    }

    private EnvironmentalReading reading(String temperature, String humidity, OffsetDateTime recordedAt) {
        EnvironmentalReading reading = new EnvironmentalReading();
        reading.setDevice(device);
        reading.setTemperatureC(new BigDecimal(temperature));
        reading.setHumidityPercent(new BigDecimal(humidity));
        reading.setRecordedAt(recordedAt);
        return reading;
    }
}
