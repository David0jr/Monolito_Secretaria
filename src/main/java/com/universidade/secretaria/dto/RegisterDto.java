package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterDto(
        @NotBlank(message = "O nome de usuário é obrigatório") String username,
        @NotBlank(message = "A senha é obrigatória") String password,
        String perfil,
        @NotBlank(message = "O nome completo é obrigatório") String nome,
        @NotBlank(message = "O CPF é obrigatório")
        @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos") String cpf,
        LocalDate dataNascimento
) {}
