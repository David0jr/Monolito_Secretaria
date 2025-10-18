package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.AlunoCadastroDto;
import com.universidade.secretaria.enums.PerfilEnum;
import com.universidade.secretaria.exception.ResourceNotFoundException;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.model.Turma;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.repository.AlunoRepository;
import com.universidade.secretaria.repository.PerfilRepository;
import com.universidade.secretaria.repository.TurmaRepository;
import com.universidade.secretaria.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlunoService {

    @Autowired private AlunoRepository alunoRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public AlunoCadastroDto salvar(@Valid AlunoCadastroDto alunoCadastroDto) {
        try {
            System.out.println("Iniciando cadastro do aluno: " + alunoCadastroDto.username());

            // Verifica se username já existe
            usuarioRepository.findByUsername(alunoCadastroDto.username()).ifPresent(usuario -> {
                throw new IllegalArgumentException("Nome de usuário já existe.");
            });

            // Busca o perfil ALUNO
            Perfil perfil = perfilRepository.findByNome(PerfilEnum.ALUNO)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil ALUNO não encontrado."));

            System.out.println("Perfil encontrado: " + perfil.getNome());

            // Cria e salva o novo Usuario
            Usuario novoUsuario = new Usuario();
            novoUsuario.setUsername(alunoCadastroDto.username());
            novoUsuario.setPassword(passwordEncoder.encode(alunoCadastroDto.password()));

            novoUsuario.setPerfis(Collections.singletonList(perfil));

            System.out.println("Salvando usuário...");
            Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
            System.out.println("Usuário salvo com ID: " + usuarioSalvo.getId());

            // Busca a turma
            System.out.println("Buscando turma com ID: " + alunoCadastroDto.turmaId());
            Turma turma = turmaRepository.findById(alunoCadastroDto.turmaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com id: " + alunoCadastroDto.turmaId()));
            System.out.println("Turma encontrada: " + turma.getPeriodo());

            // Cria e salva o aluno
            Aluno novoAluno = new Aluno();
            novoAluno.setNome(alunoCadastroDto.nome());
            novoAluno.setMatricula(alunoCadastroDto.matricula());
            novoAluno.setCpf(alunoCadastroDto.cpf());
            novoAluno.setDataNascimento(alunoCadastroDto.dataNascimento());
            novoAluno.setUsuario(usuarioSalvo);
            novoAluno.setTurma(turma);

            System.out.println("Salvando aluno...");
            Aluno alunoSalvo = alunoRepository.save(novoAluno);
            System.out.println("Aluno salvo com ID: " + alunoSalvo.getId());

            // Retorna o DTO de cadastro
            return new AlunoCadastroDto(
                    alunoSalvo.getNome(),
                    alunoSalvo.getMatricula(),
                    alunoSalvo.getCpf(),
                    alunoSalvo.getDataNascimento(),
                    alunoSalvo.getUsuario().getUsername(),
                    "", // Senha não é retornada por segurança
                    alunoSalvo.getTurma().getId()
            );

        } catch (Exception e) {
            System.err.println("ERRO no cadastro do aluno: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<AlunoCadastroDto> listarTodos() {
        try {
            return alunoRepository.findAll().stream()
                    .map(aluno -> new AlunoCadastroDto(
                            aluno.getNome(),
                            aluno.getMatricula(),
                            aluno.getCpf(),
                            aluno.getDataNascimento(),
                            aluno.getUsuario() != null ? aluno.getUsuario().getUsername() : "",
                            "", // Senha não é retornada
                            aluno.getTurma() != null ? aluno.getTurma().getId() : null
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("ERRO ao listar alunos: " + e.getMessage());
            throw e;
        }
    }

    public AlunoCadastroDto buscarPorId(Long id) {
        try {
            Aluno aluno = alunoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + id));

            return new AlunoCadastroDto(
                    aluno.getNome(),
                    aluno.getMatricula(),
                    aluno.getCpf(),
                    aluno.getDataNascimento(),
                    aluno.getUsuario() != null ? aluno.getUsuario().getUsername() : "",
                    "", // Senha não é retornada
                    aluno.getTurma() != null ? aluno.getTurma().getId() : null
            );
        } catch (Exception e) {
            System.err.println("ERRO ao buscar aluno: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public AlunoCadastroDto atualizar(Long id, @Valid AlunoCadastroDto alunoCadastroDto) {
        try {
            System.out.println("Atualizando aluno ID: " + id);

            Aluno alunoExistente = alunoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado para atualização com ID: " + id));

            // Atualiza dados básicos do aluno
            alunoExistente.setNome(alunoCadastroDto.nome());
            alunoExistente.setMatricula(alunoCadastroDto.matricula());
            alunoExistente.setCpf(alunoCadastroDto.cpf());
            alunoExistente.setDataNascimento(alunoCadastroDto.dataNascimento());

            // Atualiza usuário se existir
            Usuario usuarioAssociado = alunoExistente.getUsuario();
            if (usuarioAssociado != null) {
                if (alunoCadastroDto.username() != null && !alunoCadastroDto.username().isBlank()) {
                    // Verifica se o username já existe para outro usuário
                    usuarioRepository.findByUsername(alunoCadastroDto.username()).ifPresent(u -> {
                        if (!u.getId().equals(usuarioAssociado.getId())) {
                            throw new IllegalArgumentException("Nome de usuário já existe para outro aluno.");
                        }
                    });
                    usuarioAssociado.setUsername(alunoCadastroDto.username());
                }

                if (alunoCadastroDto.password() != null && !alunoCadastroDto.password().isBlank()) {
                    usuarioAssociado.setPassword(passwordEncoder.encode(alunoCadastroDto.password()));
                }
                usuarioRepository.save(usuarioAssociado);
            }

            // Atualiza turma
            if (alunoCadastroDto.turmaId() != null) {
                Turma turma = turmaRepository.findById(alunoCadastroDto.turmaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com id: " + alunoCadastroDto.turmaId()));
                alunoExistente.setTurma(turma);
            }

            Aluno alunoAtualizado = alunoRepository.save(alunoExistente);

            // Retorna o DTO atualizado
            return new AlunoCadastroDto(
                    alunoAtualizado.getNome(),
                    alunoAtualizado.getMatricula(),
                    alunoAtualizado.getCpf(),
                    alunoAtualizado.getDataNascimento(),
                    alunoAtualizado.getUsuario() != null ? alunoAtualizado.getUsuario().getUsername() : "",
                    "", // Senha não é retornada
                    alunoAtualizado.getTurma() != null ? alunoAtualizado.getTurma().getId() : null
            );
        } catch (Exception e) {
            System.err.println("ERRO ao atualizar aluno: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deletar(Long id) {
        try {
            Aluno aluno = alunoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado para exclusão com ID: " + id));
            alunoRepository.delete(aluno);
            System.out.println("Aluno deletado com ID: " + id);
        } catch (Exception e) {
            System.err.println("ERRO ao deletar aluno: " + e.getMessage());
            throw e;
        }
    }
}