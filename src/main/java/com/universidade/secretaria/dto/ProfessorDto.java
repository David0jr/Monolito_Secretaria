package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfessorDto(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O CPF é obrigatório")
        @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos") String cpf,
        String especializacao,
        @NotBlank(message = "O nome de usuário é obrigatório") String username,
        @NotBlank(message = "A senha é obrigatória") String password
) {}