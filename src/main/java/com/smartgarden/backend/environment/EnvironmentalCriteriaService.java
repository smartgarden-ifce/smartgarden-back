package com.smartgarden.backend.environment;

import com.smartgarden.backend.environment.dto.EnvironmentalCriteriaResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EnvironmentalCriteriaService {

    public static final BigDecimal TEMPERATURE_MIN_C = BigDecimal.valueOf(20);
    public static final BigDecimal TEMPERATURE_MAX_C = BigDecimal.valueOf(30);
    public static final BigDecimal HUMIDITY_MIN_PERCENT = BigDecimal.valueOf(40);
    public static final BigDecimal HUMIDITY_MAX_PERCENT = BigDecimal.valueOf(70);

    public EnvironmentalCriteriaResponse getCriteria() {
        return new EnvironmentalCriteriaResponse(
                TEMPERATURE_MIN_C,
                TEMPERATURE_MAX_C,
                HUMIDITY_MIN_PERCENT,
                HUMIDITY_MAX_PERCENT,
                "Temperatura agradável entre 20 e 30 °C, inclusive.",
                "Umidade normal entre 40% e 70%, inclusive.",
                "O ambiente é adequado quando temperatura e umidade estão simultaneamente dentro das faixas."
        );
    }

    public boolean isTemperatureAdequate(BigDecimal temperatureC) {
        return temperatureC.compareTo(TEMPERATURE_MIN_C) >= 0
                && temperatureC.compareTo(TEMPERATURE_MAX_C) <= 0;
    }

    public boolean isHumidityAdequate(BigDecimal humidityPercent) {
        return humidityPercent.compareTo(HUMIDITY_MIN_PERCENT) >= 0
                && humidityPercent.compareTo(HUMIDITY_MAX_PERCENT) <= 0;
    }

    public boolean isEnvironmentAdequate(BigDecimal temperatureC, BigDecimal humidityPercent) {
        return isTemperatureAdequate(temperatureC) && isHumidityAdequate(humidityPercent);
    }

    public String temperatureStatus(BigDecimal temperatureC) {
        if (temperatureC.compareTo(TEMPERATURE_MIN_C) < 0) {
            return "Fria";
        }
        if (temperatureC.compareTo(TEMPERATURE_MAX_C) > 0) {
            return "Quente";
        }
        return "Agradável";
    }

    public String humidityStatus(BigDecimal humidityPercent) {
        if (humidityPercent.compareTo(HUMIDITY_MIN_PERCENT) < 0) {
            return "Baixa";
        }
        if (humidityPercent.compareTo(HUMIDITY_MAX_PERCENT) > 0) {
            return "Alta";
        }
        return "Normal";
    }
}
