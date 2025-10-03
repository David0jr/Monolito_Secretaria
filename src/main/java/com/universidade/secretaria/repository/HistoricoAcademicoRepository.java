package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.HistoricoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoAcademicoRepository extends JpaRepository<HistoricoAcademico, Long> {

}