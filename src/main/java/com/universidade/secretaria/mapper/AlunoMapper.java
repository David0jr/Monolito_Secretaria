package com.universidade.secretaria.mapper;

import com.universidade.secretaria.dto.AlunoDto;
import com.universidade.secretaria.model.Aluno;

public class AlunoMapper {

    /**
     * Converte um AlunoDto para a entidade Aluno.
     */
    public static Aluno toEntity(AlunoDto dto) {
        if (dto == null) {
            return null;
        }

        Aluno aluno = new Aluno();
        aluno.setNome(dto.nome());
        aluno.setCpf(dto.cpf());
        aluno.setDataNascimento(dto.dataNascimento());

        return aluno;
    }

    /**
     * Converte a entidade Aluno para um AlunoDto.
     */
    public static AlunoDto toDto(Aluno entidade) {
        if (entidade == null) {
            return null;
        }

        return new AlunoDto(
                entidade.getNome(),
                entidade.getCpf(),
                entidade.getDataNascimento()
        );
    }
}