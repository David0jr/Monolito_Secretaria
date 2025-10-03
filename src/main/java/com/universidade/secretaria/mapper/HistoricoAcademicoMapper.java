package com.universidade.secretaria.mapper;

import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.HistoricoAcademico;

public class HistoricoAcademicoMapper {

    public static HistoricoAcademico toEntity(HistoricoAcademicoDto dto, Aluno aluno, Disciplina disciplina) {
        if (dto == null) {
            return null;
        }
        HistoricoAcademico historico = new HistoricoAcademico();
        historico.setNota(dto.nota());
        historico.setFrequencia(dto.frequencia());
        historico.setAluno(aluno);
        historico.setDisciplina(disciplina);
        return historico;
    }
}