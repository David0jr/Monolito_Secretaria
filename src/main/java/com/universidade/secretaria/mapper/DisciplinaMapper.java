package com.universidade.secretaria.mapper;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;

import java.util.List;


public class DisciplinaMapper {

    public static Disciplina toEntity(DisciplinaDto dto, Curso curso, List<Disciplina> preRequisitos) {
        if (dto == null) {
            return null;
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setNome(dto.nome());
        disciplina.setCargaHoraria(dto.cargaHoraria());
        disciplina.setCurso(curso);
        disciplina.setPreRequisitos(preRequisitos);

        return disciplina;
    }

    public static DisciplinaDto toDto(Disciplina entidade) {
        if (entidade == null) {
            return null;
        }

        List<Long> preRequisitosIds = null;
        if (entidade.getPreRequisitos() != null) {
            preRequisitosIds = entidade.getPreRequisitos().stream()
                    .map(Disciplina::getId)
                    .toList();
        }

        return new DisciplinaDto(
                entidade.getNome(),
                entidade.getCargaHoraria(),
                entidade.getCurso().getId(),
                preRequisitosIds
        );
    }
}