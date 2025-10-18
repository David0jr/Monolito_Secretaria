package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TurmaDto(
        Long id, @NotBlank(message = "O período é obrigatório")
        String periodo,
        Long cursoId,
        List<Long> disciplinasIds,
        List<Long> professoresIds,
        List<Long> alunosIds
) {}