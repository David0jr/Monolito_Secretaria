package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TurmaDto(
        @NotBlank(message = "O período é obrigatório") String periodo,
        List<Long> disciplinasIds

) {}