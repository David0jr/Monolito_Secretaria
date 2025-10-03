package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotNull;

public record HistoricoAcademicoDto(
        @NotNull(message = "A nota é obrigatória")
        Double nota,

        @NotNull(message = "A frequência é obrigatória")
        Double frequencia,

        @NotNull(message = "O ID do aluno é obrigatório")
        Long alunoId,

        @NotNull(message = "O ID da disciplina é obrigatório")
        Long disciplinaId
) {}