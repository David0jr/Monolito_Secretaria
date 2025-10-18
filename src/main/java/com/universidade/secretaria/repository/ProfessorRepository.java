package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {


    Optional<Professor> findByCpf(String cpf);
    Optional<Professor> findByNome(String nome);
}