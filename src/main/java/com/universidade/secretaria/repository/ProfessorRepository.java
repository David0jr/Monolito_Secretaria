package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Professor findByCpf(String cpf);
}
