package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.RegisterDto;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import com.universidade.secretaria.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
        if (usuarioRepository.findByUsername(registerDto.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Nome de usuário já existe!");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(registerDto.username());
        usuario.setPassword(passwordEncoder.encode(registerDto.password()));

        String perfilName = registerDto.perfil() != null ? registerDto.perfil() : "ROLE_ALUNO";
        Optional<Perfil> perfilOpt = perfilRepository.findByNome(PerfilEnum.valueOf(perfilName));

        if (perfilOpt.isPresent()) {
            usuario.setPerfils(List.of(perfilOpt.get()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Perfil não encontrado.");
        }

        if (perfilName.equals("ROLE_ALUNO")) {
            Aluno novoAluno = new Aluno();
            novoAluno.setNome(registerDto.nome());
            novoAluno.setCpf(registerDto.cpf());
            novoAluno.setDataNascimento(registerDto.dataNascimento());
            novoAluno.setUsuario(usuario);

            // --- A LINHA ABAIXO FOI ADICIONADA ---
            novoAluno.setMatricula("MAT-" + System.currentTimeMillis());

            alunoRepository.save(novoAluno);
        } else {
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}