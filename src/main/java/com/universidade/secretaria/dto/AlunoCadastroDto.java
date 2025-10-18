package com.universidade.secretaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AlunoCadastroDto(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @NotBlank(message = "A matrícula é obrigatória")
        String matricula,
        @NotBlank(message = "O CPF é obrigatório")
        @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos")
        String cpf,
        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate dataNascimento,
        @NotBlank(message = "O nome de usuário é obrigatório")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        String password,
        @NotNull(message = "O ID da turma é obrigatório")
        Long turmaId
) {
    // Construtor adicional se necessário para casos onde a senha pode ser vazia
    public AlunoCadastroDto(String nome, String matricula, String cpf, LocalDate dataNascimento,
                            String username, String password, Long turmaId) {
        this.nome = nome;
        this.matricula = matricula;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.username = username;
        this.password = password;
        this.turmaId = turmaId;
    }
}