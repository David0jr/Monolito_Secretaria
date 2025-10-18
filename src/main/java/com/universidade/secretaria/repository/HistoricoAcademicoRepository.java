package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.HistoricoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoAcademicoRepository extends JpaRepository<HistoricoAcademico, Long> {

    Optional<HistoricoAcademico> findByAlunoAndDisciplina(Aluno aluno, Disciplina disciplina);
    List<HistoricoAcademico> findByAluno(Aluno aluno);
    List<HistoricoAcademico> findByDisciplina(Disciplina disciplina);

}