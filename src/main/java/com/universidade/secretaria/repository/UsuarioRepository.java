package com.universidade.secretaria.repository;

import com.universidade.secretaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /*Método para a autenticação. O Spring Security usará ele
     para buscar um usuário pelo nome de usuário.*/

    Optional<Usuario> findByUsername(String username);
}
