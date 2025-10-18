package com.universidade.secretaria.dto;

import java.util.List;
import java.util.Set;

public record DisciplinaDto(
        Long id,
        String nome,
        int cargaHoraria,
        Set<Long> cursosIds, // Agora um SET de IDs de cursos
        List<String> nomesCursos, // Lista de nomes dos cursos
        Long professorId,
        String nomeProfessor,
        Set<Long> preRequisitosIds,
        List<String> nomesPreRequisitos // List de nomes dos pré-requisitos
) {
}