package com.universidade.secretaria.repository;


import com.universidade.secretaria.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    Optional<Turma> findById(Long id);
    Optional<Turma> findByPeriodo(String periodo);
    List<Turma> findByDisciplinasId(Long disciplinaId);
    List<Turma> findByProfessoresId(Long professorId);
    List<Turma> findByAlunosId(Long alunoId);
}