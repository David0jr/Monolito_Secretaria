package com.universidade.secretaria.repository;

import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(PerfilEnum nome);

    PerfilEnum nome(PerfilEnum nome);
}