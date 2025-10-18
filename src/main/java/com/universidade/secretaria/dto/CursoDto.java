package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;


public record CursoDto(
        Long id,
        @NotBlank(message = "O nome do curso não pode estar em branco")
        @Size(max = 100, message = "O nome do curso deve ter no máximo 100 caracteres")
        String nome,
        String descricao,
        Set<Long> disciplinasIds,
        List<DisciplinaDto> disciplinas //
) {}