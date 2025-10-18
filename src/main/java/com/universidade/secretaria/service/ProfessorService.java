package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.dto.ProfessorRegistroDTO;
import com.universidade.secretaria.enums.PerfilEnum; // Importe PerfilEnum
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

    @Autowired private ProfessorRepository professorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public ProfessorRegistroDTO salvar(ProfessorRegistroDTO professorRegistroDTO) {
        // 1. Validação de unicidade de username
        usuarioRepository.findByUsername(professorRegistroDTO.username()).ifPresent(usuario -> {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        });

        // 2. Busca o perfil PROFESSOR
        // Agora passando PerfilEnum.ROLE_PROFESSOR como argumento
        Perfil perfil = perfilRepository.findByNome(PerfilEnum.PROFESSOR) // <--- CORREÇÃO AQUI
                .orElseThrow(() -> new NoSuchElementException("Perfil PROFESSOR não encontrado."));

        // 3. Cria e configura o novo Usuário
        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(professorRegistroDTO.username());
        novoUsuario.setPassword(passwordEncoder.encode(professorRegistroDTO.password()));
        // setPerfis espera List<Perfil>, e Collections.singletonList retorna uma List
        novoUsuario.setPerfis(Collections.singletonList(perfil));

        // 4. Cria e configura o novo Professor
        Professor novoProfessor = new Professor(
                professorRegistroDTO.nome(),
                professorRegistroDTO.cpf(),
                professorRegistroDTO.especializacao(),
                novoUsuario // Associa o usuário ao professor
        );

        // 5. Salva o professor (que cascateia o salvamento do usuário)
        Professor professorSalvo = professorRepository.save(novoProfessor);
        return toDto(professorSalvo);
    }

    public List<ProfessorRegistroDTO> listarTodos() {
        return professorRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProfessorRegistroDTO buscarPorId(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado."));
        return toDto(professor);
    }

    @Transactional
    public ProfessorRegistroDTO atualizar(Long id, ProfessorRegistroDTO professorDto) {
        Professor professorExistente = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado para atualização."));

        // Atualiza os dados do professor
        professorExistente.setNome(professorDto.nome());
        professorExistente.setCpf(professorDto.cpf());
        professorExistente.setEspecializacao(professorDto.especializacao());

        // Atualiza os dados do usuário associado (se fornecidos)
        if (professorDto.username() != null && !professorDto.username().isBlank()) {
            // Verifica se o novo username já existe para outro usuário
            usuarioRepository.findByUsername(professorDto.username()).ifPresent(u -> {
                if (!u.getId().equals(professorExistente.getUsuario().getId())) {
                    throw new IllegalArgumentException("Nome de usuário já existe para outro professor.");
                }
            });
            professorExistente.getUsuario().setUsername(professorDto.username());
        }
        if (professorDto.password() != null && !professorDto.password().isBlank()) {
            professorExistente.getUsuario().setPassword(passwordEncoder.encode(professorDto.password()));
        }

        Professor professorAtualizado = professorRepository.save(professorExistente);
        return toDto(professorAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        // Encontra o professor para garantir que ele existe antes de deletar
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado para exclusão."));

        professorRepository.deleteById(id);
    }

    // Assumindo que ProfessorRegistroDTO não tem o ID do professor para simplificar este toDto
    private ProfessorRegistroDTO toDto(Professor professor) {
        return new ProfessorRegistroDTO(
                professor.getId(),
                professor.getUsuario().getUsername(),
                null, // A senha nunca deve ser retornada em um DTO de listagem/visualização
                professor.getNome(),
                professor.getCpf(),
                professor.getEspecializacao()
        );
    }

    public Set<DisciplinaDto> getDisciplinasByProfessorId(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado."));

        // Se professor.getDisciplinas() puder retornar null, proteja com Optional.ofNullable ou um if
        return professor.getDisciplinas() != null ?
                professor.getDisciplinas().stream()
                        .map(d -> {
                            // Prepara os Sets de IDs e nomes de pré-requisitos, com tratamento para null
                            Set<Long> preRequisitosIds = (d.getPreRequisitos() != null) ?
                                    d.getPreRequisitos().stream().map(Disciplina::getId).collect(Collectors.toSet())
                                    : new HashSet<>();

                            List<String> nomesPreRequisitos = (d.getPreRequisitos() != null) ?
                                    d.getPreRequisitos().stream().map(Disciplina::getNome).collect(Collectors.toList())
                                    : List.of(); // Usar List.of() para lista imutável vazia

                            Set<Long> disciplinaCursosIds = (d.getCursos() != null) ?
                                    d.getCursos().stream().map(Curso::getId).collect(Collectors.toSet())
                                    : new HashSet<>();

                            List<String> disciplinaNomesCursos = (d.getCursos() != null) ?
                                    d.getCursos().stream().map(Curso::getNome).collect(Collectors.toList())
                                    : List.of("Não Atribuído"); // Ou List.of() se preferir lista vazia


                            // Agora chame o construtor completo do record DisciplinaDto
                            return new DisciplinaDto(
                                    d.getId(),
                                    d.getNome(),
                                    d.getCargaHoraria(),
                                    disciplinaCursosIds, // Passa o Set de IDs de Cursos da DISCIPLINA
                                    disciplinaNomesCursos, // Passa a List de Nomes de Cursos da DISCIPLINA
                                    d.getProfessor() != null ? d.getProfessor().getId() : null, // Protege contra professor null
                                    d.getProfessor() != null ? d.getProfessor().getNome() : "Não Atribuído", // Protege contra professor null
                                    preRequisitosIds,
                                    nomesPreRequisitos
                            );
                        })
                        .collect(Collectors.toSet())
                : Collections.emptySet(); // Se não há disciplinas, retorna um Set vazio
    }
}