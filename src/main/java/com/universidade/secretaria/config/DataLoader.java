package com.universidade.secretaria.config;

import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Importe esta anotação

import java.util.List;
import java.util.Optional;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Garante que tudo execute na mesma transação
    public void run(String... args) throws Exception {
        // Encontra ou cria os perfis e os armazena
        Perfil alunoPerfil = findOrCreatePerfil(PerfilEnum.ROLE_ALUNO);
        Perfil professorPerfil = findOrCreatePerfil(PerfilEnum.ROLE_PROFESSOR);
        Perfil secretariaPerfil = findOrCreatePerfil(PerfilEnum.ROLE_SECRETARIA);

        // Cria o usuário da secretaria apenas se ele não existir
        Optional<Usuario> secretariaUsuarioOpt = usuarioRepository.findByUsername("secretaria");
        if (secretariaUsuarioOpt.isEmpty()) {
            Usuario secretariaUsuario = new Usuario();
            secretariaUsuario.setUsername("secretaria");
            secretariaUsuario.setPassword(passwordEncoder.encode("senha123"));
            secretariaUsuario.setPerfils(List.of(secretariaPerfil));

            usuarioRepository.save(secretariaUsuario);
            System.out.println("Usuário 'secretaria' criado com sucesso!");
        }
    }

    private Perfil findOrCreatePerfil(PerfilEnum nome) {
        return perfilRepository.findByNome(nome).orElseGet(() -> {
            Perfil newPerfil = new Perfil();
            newPerfil.setNome(nome);
            return perfilRepository.save(newPerfil);
        });
    }
}