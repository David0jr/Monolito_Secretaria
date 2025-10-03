package com.universidade.secretaria.mapper;

import jakarta.persistence.JoinColumn;
import org.springframework.stereotype.Component;
import com.universidade.secretaria.dto.ProfessorDto;
import com.universidade.secretaria.model.Professor;


@Component
public class ProfessorMapper {


    public static Professor toEntity(ProfessorDto dto) {
        if (dto == null) {
            return null;
        }

        Professor professor = new Professor();
        professor.setNome(dto.nome());
        professor.setCpf(dto.cpf());
        professor.setEspecializacao(dto.especializacao());

        return professor;
    }

    /*
      Converte a entidade Professor para um ProfessorDto.
     */

    public static ProfessorDto toDto(Professor entidade) {
        if (entidade == null) {
            return null;
        }

        return new ProfessorDto(
                entidade.getNome(),
                entidade.getCpf(),
                entidade.getEspecializacao(),
                null,
                null
        );
    }
}
