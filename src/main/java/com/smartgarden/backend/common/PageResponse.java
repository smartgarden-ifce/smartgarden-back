package com.smartgarden.backend.common;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Resposta paginada")
public record PageResponse<T>(
        @Schema(description = "Lista de itens da pagina atual")
        List<T> content,
        @Schema(example = "0")
        int page,
        @Schema(example = "20")
        int size,
        @Schema(example = "125")
        long totalElements,
        @Schema(example = "7")
        int totalPages,
        @Schema(example = "true")
        boolean first,
        @Schema(example = "false")
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

