package com.universidade.secretaria.init;

import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired private PerfilRepository perfilRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override @Transactional
    public void run(String... args) throws Exception {
        Perfil alunoPerfil = findOrCreatePerfil(PerfilEnum.ALUNO);
        Perfil professorPerfil = findOrCreatePerfil(PerfilEnum.PROFESSOR);
        Perfil secretariaPerfil = findOrCreatePerfil(PerfilEnum.SECRETARIA);

        Optional<Usuario> secretariaUsuarioOpt = usuarioRepository.findByUsername("secretaria");
        if (secretariaUsuarioOpt.isEmpty()) {
            Usuario secretariaUsuario = new Usuario();
            secretariaUsuario.setUsername("secretaria");
            secretariaUsuario.setPassword(passwordEncoder.encode("senha123"));
            secretariaUsuario.setPerfis(List.of(secretariaPerfil));

            usuarioRepository.save(secretariaUsuario);
            System.out.println("Usuário 'secretaria' criado com sucesso!");
        }
    }

    private Perfil findOrCreatePerfil(PerfilEnum perfilEnum) {
        // findByNome(String) espera o nome do enum como String
        return perfilRepository.findByNome(perfilEnum).orElseGet(() -> {
            Perfil newPerfil = new Perfil();
            newPerfil.setNome(perfilEnum);
            return perfilRepository.save(newPerfil);
        });
    }
}