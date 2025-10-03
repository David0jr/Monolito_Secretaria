package com.universidade.secretaria.mapper;

import com.universidade.secretaria.dto.TurmaDto;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.model.Turma;

import java.util.List;

public class TurmaMapper {

    public static Turma toEntity(TurmaDto dto, List<Disciplina> disciplinas) {
        if (dto == null) return null;
        Turma turma = new Turma();
        turma.setPeriodo(dto.periodo());
        turma.setDisciplinas(disciplinas);
        return turma;
    }

    public static TurmaDto toDto(Turma entidade) {
        if (entidade == null) return null;
        List<Long> disciplinasIds = null;
        if (entidade.getDisciplinas() != null) {
            disciplinasIds = entidade.getDisciplinas().stream().map(Disciplina::getId).toList();
        }
        return new TurmaDto(entidade.getPeriodo(), disciplinasIds);
    }
}