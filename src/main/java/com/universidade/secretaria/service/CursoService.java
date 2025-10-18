package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.CursoDto;
import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.repository.CursoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DisciplinaService disciplinaService; // Para converter Disciplina para DisciplinaDto
    private final DisciplinaRepository disciplinaRepository;

    @Autowired
    public CursoService(CursoRepository cursoRepository, DisciplinaService disciplinaService, DisciplinaRepository disciplinaRepository) {
        this.cursoRepository = cursoRepository;
        this.disciplinaService = disciplinaService;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional
    public CursoDto salvar(CursoDto cursoDto) {
        Curso curso = new Curso();
        curso.setNome(cursoDto.nome());
        curso.setDescricao(cursoDto.descricao());

        if (cursoDto.disciplinasIds() != null && !cursoDto.disciplinasIds().isEmpty()) {
            Set<Disciplina> disciplinas = cursoDto.disciplinasIds().stream()
                    .map(disciplinaId -> disciplinaRepository.findById(disciplinaId)
                            .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada com ID: " + disciplinaId)))
                    .collect(Collectors.toSet());
            curso.setDisciplinas(disciplinas); // Assume que Curso tem setDisciplinas
        }

        Curso savedCurso = cursoRepository.save(curso);
        return toDto(savedCurso);
    }

    @Transactional
    public CursoDto atualizar(Long id, CursoDto cursoDto) {
        Curso cursoExistente = cursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado com ID: " + id));

        cursoExistente.setNome(cursoDto.nome());
        cursoExistente.setDescricao(cursoDto.descricao());

        Set<Disciplina> novasDisciplinas = new HashSet<>();
        if (cursoDto.disciplinasIds() != null && !cursoDto.disciplinasIds().isEmpty()) {
            novasDisciplinas = cursoDto.disciplinasIds().stream()
                    .map(discId -> disciplinaRepository.findById(discId)
                            .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada com ID: " + discId)))
                    .collect(Collectors.toSet());
        }
        cursoExistente.setDisciplinas(novasDisciplinas);

        Curso updatedCurso = cursoRepository.save(cursoExistente);
        return toDto(updatedCurso);
    }

    @Transactional(readOnly = true)
    public List<CursoDto> listarTodos() {
        return cursoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CursoDto buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado com ID: " + id));
        return toDto(curso);
    }

    @Transactional
    public void deletar(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado para exclusão."));
        // Se Curso tem OneToMany com Disciplina e Turma com cascade = ALL + orphanRemoval = true,
        // a exclusão do curso pai também excluirá as disciplinas e turmas filhas.
        // Se você não quiser esse comportamento, remova cascade = ALL da entidade Curso
        cursoRepository.delete(curso);
    }

    private CursoDto toDto(Curso curso) {
        // Mapeia as disciplinas associadas para DTOs (apenas para exibição)
        List<DisciplinaDto> disciplinasDto = curso.getDisciplinas().stream()
                .map(disciplinaService::convertToDto) // Usar o serviço de disciplina para converter
                .collect(Collectors.toList());

        return new CursoDto(
                curso.getId(),
                curso.getNome(),
                curso.getDescricao(),
                curso.getDisciplinas().stream().map(Disciplina::getId).collect(Collectors.toSet()), // Mapeia IDs para o DTO de retorno também
                disciplinasDto
        );
    }
}