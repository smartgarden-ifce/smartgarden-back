package com.smartgarden.backend.reading;

import com.smartgarden.backend.common.ApiErrorResponse;
import com.smartgarden.backend.common.PageResponse;
import com.smartgarden.backend.reading.dto.CreateReadingRequest;
import com.smartgarden.backend.reading.dto.ReadingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/readings")
@Validated
@Tag(name = "Readings", description = "Ingestao e consulta de leituras ambientais")
public class EnvironmentalReadingController {

    private final EnvironmentalReadingService readingService;

    public EnvironmentalReadingController(EnvironmentalReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar leitura", description = "Recebe uma leitura enviada pelo ESP32 e persiste no banco.")
    @ApiResponse(responseCode = "201", description = "Leitura registrada com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Payload invalido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ReadingResponse createReading(@Valid @RequestBody CreateReadingRequest request) {
        return readingService.createReading(request);
    }

    @GetMapping
    @Operation(summary = "Listar leituras", description = "Consulta leituras com filtros opcionais e paginação.")
    public PageResponse<ReadingResponse> listReadings(
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Pagina da consulta, iniciando em 0", example = "0")
            @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Quantidade de itens por pagina", example = "20")
            @Min(1) @Max(200) int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endAt
    ) {
        return readingService.listReadings(deviceCode, page, size, startAt, endAt);
    }

    @GetMapping("/latest")
    @Operation(summary = "Buscar ultima leitura", description = "Retorna a leitura mais recente para um dispositivo especifico.")
    @ApiResponse(
            responseCode = "404",
            description = "Nenhuma leitura encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ReadingResponse getLatestReading(@RequestParam String deviceCode) {
        return readingService.getLatestReading(deviceCode);
    }
}
