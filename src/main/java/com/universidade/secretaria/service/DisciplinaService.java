package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.repository.CursoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;
    private final CursoRepository cursoRepository;

    @Autowired
    public DisciplinaService(DisciplinaRepository disciplinaRepository, ProfessorRepository professorRepository, CursoRepository cursoRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    public DisciplinaDto salvar(DisciplinaDto disciplinaDto) {
        // Busca Professor
        Professor professor = professorRepository.findById(disciplinaDto.professorId())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado com ID: " + disciplinaDto.professorId()));

        // Busca Cursos
        Set<Curso> cursos = new HashSet<>();
        if (disciplinaDto.cursosIds() != null && !disciplinaDto.cursosIds().isEmpty()) {
            cursos = disciplinaDto.cursosIds().stream()
                    .map(cursoId -> cursoRepository.findById(cursoId)
                            .orElseThrow(() -> new NoSuchElementException("Curso não encontrado com ID: " + cursoId)))
                    .collect(Collectors.toSet());
        }

        // Busca Pré-requisitos
        Set<Disciplina> preRequisitos = new HashSet<>();
        if (disciplinaDto.preRequisitosIds() != null && !disciplinaDto.preRequisitosIds().isEmpty()) {
            preRequisitos = disciplinaDto.preRequisitosIds().stream()
                    .map(preRequisitoId -> disciplinaRepository.findById(preRequisitoId)
                            .orElseThrow(() -> new NoSuchElementException("Pré-requisito não encontrado com ID: " + preRequisitoId)))
                    .collect(Collectors.toSet());
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setNome(disciplinaDto.nome());
        disciplina.setCargaHoraria(disciplinaDto.cargaHoraria());
        disciplina.setProfessor(professor);
        disciplina.setCursos(cursos); // Define o Set de Cursos
        disciplina.setPreRequisitos(preRequisitos);

        // --- INÍCIO DA CORREÇÃO ---

        // Sincroniza a relação Many-to-Many para o lado "dono" (Curso)
        cursos.forEach(curso -> curso.getDisciplinas().add(disciplina));

        // --- FIM DA CORREÇÃO ---

        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);
        return convertToDto(savedDisciplina);
    }

    @Transactional
    public DisciplinaDto associarProfessor(Long disciplinaId, Long professorId) {
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada com ID: " + disciplinaId));
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado com ID: " + professorId));

        disciplina.setProfessor(professor);
        Disciplina updatedDisciplina = disciplinaRepository.save(disciplina);
        return convertToDto(updatedDisciplina);
    }

    @Transactional(readOnly = true)
    public List<DisciplinaDto> listarTodos() {
        return disciplinaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DisciplinaDto buscarPorId(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada com ID: " + id));
        return convertToDto(disciplina);
    }

    @Transactional
    public DisciplinaDto atualizar(Long id, DisciplinaDto disciplinaDto) {
        Disciplina disciplinaExistente = disciplinaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada com ID: " + id));

        // Busca e atualiza o Professor
        Professor professor = professorRepository.findById(disciplinaDto.professorId())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado com ID: " + disciplinaDto.professorId()));

        // Busca e atualiza os Cursos
        Set<Curso> novosCursos = new HashSet<>();
        if (disciplinaDto.cursosIds() != null && !disciplinaDto.cursosIds().isEmpty()) {
            novosCursos = disciplinaDto.cursosIds().stream()
                    .map(cursoId -> cursoRepository.findById(cursoId)
                            .orElseThrow(() -> new NoSuchElementException("Curso não encontrado com ID: " + cursoId)))
                    .collect(Collectors.toSet());
        }

        // Atualiza Pré-requisitos
        Set<Disciplina> novosPreRequisitos = new HashSet<>();
        if (disciplinaDto.preRequisitosIds() != null && !disciplinaDto.preRequisitosIds().isEmpty()) {
            novosPreRequisitos = disciplinaDto.preRequisitosIds().stream()
                    .map(preRequisitoId -> disciplinaRepository.findById(preRequisitoId)
                            .orElseThrow(() -> new NoSuchElementException("Pré-requisito não encontrado com ID: " + preRequisitoId)))
                    .collect(Collectors.toSet());
        }

        disciplinaExistente.setNome(disciplinaDto.nome());
        disciplinaExistente.setCargaHoraria(disciplinaDto.cargaHoraria());
        disciplinaExistente.setProfessor(professor);
        disciplinaExistente.setCursos(novosCursos); // Atualiza o Set de Cursos
        disciplinaExistente.setPreRequisitos(novosPreRequisitos); // Atualiza os pré-requisitos

        Disciplina updatedDisciplina = disciplinaRepository.save(disciplinaExistente);
        return convertToDto(updatedDisciplina);
    }

    @Transactional
    public void deletar(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new NoSuchElementException("Disciplina não encontrada com ID: " + id);
        }
        disciplinaRepository.deleteById(id);
    }

    // Método auxiliar para converter Entidade para DTO
    DisciplinaDto convertToDto(Disciplina disciplina) {
        // Cursos da disciplina (ManyToMany)
        Set<Long> cursosIds = (disciplina.getCursos() != null) ?
                disciplina.getCursos().stream()
                        .map(Curso::getId)
                        .collect(Collectors.toSet())
                : new HashSet<>();

        List<String> nomesCursos = (disciplina.getCursos() != null) ?
                disciplina.getCursos().stream()
                        .map(Curso::getNome)
                        .collect(Collectors.toList())
                : List.of("Não Atribuído"); // Pode ser uma lista vazia ou 'Não Atribuído' se não houver cursos

        String nomeProfessor = (disciplina.getProfessor() != null) ? disciplina.getProfessor().getNome() : "Não Atribuído";
        Long professorId = (disciplina.getProfessor() != null) ? disciplina.getProfessor().getId() : null;

        List<String> nomesPreRequisitos = (disciplina.getPreRequisitos() != null) ?
                disciplina.getPreRequisitos().stream()
                        .map(Disciplina::getNome)
                        .collect(Collectors.toList())
                : List.of();

        Set<Long> preRequisitosIds = (disciplina.getPreRequisitos() != null) ?
                disciplina.getPreRequisitos().stream()
                        .map(Disciplina::getId)
                        .collect(Collectors.toSet())
                : new HashSet<>();

        return new DisciplinaDto(
                disciplina.getId(),
                disciplina.getNome(),
                disciplina.getCargaHoraria(),
                cursosIds, // Set de IDs de Cursos
                nomesCursos, // List de Nomes de Cursos
                professorId,
                nomeProfessor,
                preRequisitosIds,
                nomesPreRequisitos
        );
    }
}