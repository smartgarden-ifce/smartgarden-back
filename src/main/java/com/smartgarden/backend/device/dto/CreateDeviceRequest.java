package com.smartgarden.backend.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @NotBlank
        @Size(max = 100)
        String deviceCode,

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 200)
        String location
) {
}

