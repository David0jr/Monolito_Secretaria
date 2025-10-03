package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DisciplinaDto(
        @NotBlank(message = "O nome da disciplina é obrigatório")
        String nome,

        @NotNull(message = "A carga horária é obrigatória")
        Integer cargaHoraria,

        @NotNull(message = "O curso é obrigatório")
        Long cursoId,

        List<Long> preRequisitosIds) {
}
