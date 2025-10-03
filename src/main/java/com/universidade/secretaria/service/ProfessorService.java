package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.ProfessorDto;
import com.universidade.secretaria.mapper.ProfessorMapper;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    @Autowired private ProfessorRepository professorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ProfessorMapper professorMapper;


    // O método 'salvar' agora lida com a criação completa
    public Professor salvar(ProfessorDto professorDto) {
        if (usuarioRepository.findByUsername(professorDto.username()).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(professorDto.username());
        novoUsuario.setPassword(passwordEncoder.encode(professorDto.password()));

        Optional<Perfil> perfilOpt = perfilRepository.findByNome(PerfilEnum.ROLE_PROFESSOR);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil de Professor não encontrado.");
        }
        novoUsuario.setPerfils(List.of(perfilOpt.get()));

        Professor novoProfessor = ProfessorMapper.toEntity(professorDto);
        novoProfessor.setUsuario(novoUsuario);

        return professorRepository.save(novoProfessor);
    }



            //Métodos Adicionais de CRUD

    public List<Professor> listarTodos() {
        return professorRepository.findAll();
    }

    public Optional<Professor> buscarPorId(Long id) {
        return professorRepository.findById(id);
    }

    public Professor atualizar(Long id, ProfessorDto professorDto) {
        Professor professorExistente = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));

        professorExistente.setNome(professorDto.nome());
        professorExistente.setCpf(professorDto.cpf());
        professorExistente.setEspecializacao(professorDto.especializacao());

        // --- LÓGICA CORRIGIDA PARA CRIAR O USUÁRIO SE ELE NÃO EXISTIR ---
        Usuario usuario = null;
        if (usuario == null) {
            usuario = new Usuario();
            professorExistente.setUsuario(usuario);

            Optional<Perfil> perfilOpt = perfilRepository.findByNome(PerfilEnum.ROLE_PROFESSOR);
            Usuario finalUsuario = usuario;
            perfilOpt.ifPresent(perfil -> finalUsuario.setPerfils(List.of(perfil)));
        } else {
            usuario = professorExistente.getUsuario();
        }

        if (professorDto.username() != null && !professorDto.username().isBlank()) {
            usuario.setUsername(professorDto.username());
        }

        if (professorDto.password() != null && !professorDto.password().isBlank()) {
            String encodedPassword = passwordEncoder.encode(professorDto.password());
            usuario.setPassword(encodedPassword);
        }
        // --- FIM DA LÓGICA CORRIGIDA ---

        return professorRepository.save(professorExistente);
    }

    public void deletar(Long id) {
        if (!professorRepository.existsById(id)) {
            throw new IllegalArgumentException("Professor não encontrado para exclusão.");
        }
        professorRepository.deleteById(id);
    }
}