package com.smartgarden.backend.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @Schema(description = "Codigo unico do dispositivo", example = "esp32-jardim-bloco-a")
        @NotBlank
        @Size(max = 100)
        String deviceCode,

        @Schema(description = "Nome amigavel do dispositivo", example = "ESP32 Jardim Bloco A")
        @NotBlank
        @Size(max = 120)
        String name,

        @Schema(description = "Localizacao fisica do dispositivo", example = "Universidade - Bloco A")
        @Size(max = 200)
        String location
) {
}
