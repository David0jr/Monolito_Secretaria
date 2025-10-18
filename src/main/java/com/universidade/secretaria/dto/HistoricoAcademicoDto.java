package com.universidade.secretaria.dto;

import com.universidade.secretaria.model.HistoricoAcademico;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HistoricoAcademicoDto(
        Long id,
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 0, message = "A nota não pode ser negativa")
        @Max(value = 10, message = "A nota máxima é 10")
        Double nota,
        @NotNull(message = "A frequência é obrigatória")
        @Min(value = 0, message = "A frequência não pode ser negativa")
        @Max(value = 100, message = "A frequência máxima é 100")
        Double frequencia,
        @NotNull(message = "O ID do aluno é obrigatório")
        Long alunoId,
        @NotNull(message = "O ID da disciplina é obrigatório")
        Long disciplinaId
) {
    public HistoricoAcademicoDto(HistoricoAcademico historico) {
        this(
                historico.getId(),
                historico.getNota(),
                historico.getFrequencia(),
                historico.getAluno() != null ? historico.getAluno().getId() : null,
                historico.getDisciplina() != null ? historico.getDisciplina().getId() : null
        );
    }
}