package com.smartgarden.backend.environment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentalCriteriaServiceTest {

    private final EnvironmentalCriteriaService service = new EnvironmentalCriteriaService();

    @Test
    void shouldTreatAllBoundariesAsAdequate() {
        assertThat(service.isEnvironmentAdequate(value("20"), value("40"))).isTrue();
        assertThat(service.isEnvironmentAdequate(value("30"), value("70"))).isTrue();
    }

    @Test
    void shouldClassifyValuesOutsideTheRanges() {
        assertThat(service.temperatureStatus(value("19.99"))).isEqualTo("Fria");
        assertThat(service.temperatureStatus(value("30.01"))).isEqualTo("Quente");
        assertThat(service.humidityStatus(value("39.99"))).isEqualTo("Baixa");
        assertThat(service.humidityStatus(value("70.01"))).isEqualTo("Alta");
        assertThat(service.isEnvironmentAdequate(value("25"), value("39"))).isFalse();
    }

    private BigDecimal value(String value) {
        return new BigDecimal(value);
    }
}
