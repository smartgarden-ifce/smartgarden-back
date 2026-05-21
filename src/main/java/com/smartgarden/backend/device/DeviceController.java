package com.smartgarden.backend.device;

import com.smartgarden.backend.common.ApiErrorResponse;
import com.smartgarden.backend.device.dto.CreateDeviceRequest;
import com.smartgarden.backend.device.dto.DeviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Cadastro e consulta de dispositivos ESP32")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Listar dispositivos", description = "Retorna todos os dispositivos cadastrados ordenados por nome.")
    public List<DeviceResponse> listDevices() {
        return deviceService.listDevices();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar dispositivo", description = "Cria manualmente um dispositivo para associar futuras leituras.")
    @ApiResponse(responseCode = "201", description = "Dispositivo criado com sucesso")
    @ApiResponse(
            responseCode = "409",
            description = "Ja existe dispositivo com o codigo informado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DeviceResponse createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return deviceService.createDevice(request);
    }
}
