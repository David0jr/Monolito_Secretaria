package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    Optional<Disciplina> findByNome(String nome);
    List<Disciplina> findByNomeContainingIgnoreCase(String nome);
    List<Disciplina> findByCursos(Curso curso);
}