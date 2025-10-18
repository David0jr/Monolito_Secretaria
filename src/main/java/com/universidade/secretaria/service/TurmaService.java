package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.TurmaDto;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.model.Turma;
import com.universidade.secretaria.repository.AlunoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import com.universidade.secretaria.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TurmaService {

    private static final Logger logger = LoggerFactory.getLogger(TurmaService.class);
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    public TurmaService(TurmaRepository turmaRepository, DisciplinaRepository disciplinaRepository,
                        ProfessorRepository professorRepository, AlunoRepository alunoRepository) {
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public TurmaDto salvar(TurmaDto turmaDto) {
        logger.info("Salvando turma: {}", turmaDto);

        // Verifica se já existe turma com mesmo período
        turmaRepository.findByPeriodo(turmaDto.periodo()).ifPresent(t -> {
            throw new IllegalArgumentException("Já existe uma turma para este período.");
        });

        // Inicializa listas para evitar NullPointerException
        List<Long> disciplinasIds = turmaDto.disciplinasIds() != null ? turmaDto.disciplinasIds() : new ArrayList<>();
        List<Long> professoresIds = turmaDto.professoresIds() != null ? turmaDto.professoresIds() : new ArrayList<>();
        List<Long> alunosIds = turmaDto.alunosIds() != null ? turmaDto.alunosIds() : new ArrayList<>();

        // Busca as entidades relacionadas (apenas se houver IDs)
        Set<Disciplina> disciplinas = new HashSet<>();
        if (!disciplinasIds.isEmpty()) {
            disciplinas = new HashSet<>(disciplinaRepository.findAllById(disciplinasIds));
            if (disciplinas.size() != disciplinasIds.size()) {
                throw new IllegalArgumentException("IDs de Disciplina inválidos. Algumas disciplinas não foram encontradas.");
            }
        }

        Set<Professor> professores = new HashSet<>();
        if (!professoresIds.isEmpty()) {
            professores = new HashSet<>(professorRepository.findAllById(professoresIds));
            if (professores.size() != professoresIds.size()) {
                throw new IllegalArgumentException("IDs de Professor inválidos. Alguns professores não foram encontrados.");
            }
        }

        Set<Aluno> alunos = new HashSet<>();
        if (!alunosIds.isEmpty()) {
            alunos = new HashSet<>(alunoRepository.findAllById(alunosIds));
            if (alunos.size() != alunosIds.size()) {
                throw new IllegalArgumentException("IDs de Aluno inválidos. Alguns alunos não foram encontrados.");
            }
        }

        // Cria e salva a turma
        Turma turma = new Turma(turmaDto.periodo());
        turma.setDisciplinas(disciplinas);
        turma.setProfessores(professores);
        turma.setAlunos(alunos);

        Turma turmaSalva = turmaRepository.save(turma);
        logger.info("Turma salva com ID: {}", turmaSalva.getId());

        return toDto(turmaSalva);
    }

    public List<TurmaDto> listarTodos() {
        logger.info("Listando todas as turmas");
        return turmaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TurmaDto buscarPorId(Long id) {
        logger.info("Buscando turma com ID: {}", id);
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turma não encontrada com ID: " + id));
        return toDto(turma);
    }

    @Transactional
    public TurmaDto atualizar(Long id, TurmaDto turmaDto) {
        logger.info("Atualizando turma ID: {} com dados: {}", id, turmaDto);

        Turma turmaExistente = turmaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turma não encontrada para atualização com ID: " + id));

        // Verifica se outro período conflita (exceto a própria turma)
        turmaRepository.findByPeriodo(turmaDto.periodo())
                .ifPresent(t -> {
                    if (!t.getId().equals(id)) {
                        throw new IllegalArgumentException("Já existe outra turma com este período.");
                    }
                });

        turmaExistente.setPeriodo(turmaDto.periodo());

        // Inicializa listas para evitar NullPointerException
        List<Long> disciplinasIds = turmaDto.disciplinasIds() != null ? turmaDto.disciplinasIds() : new ArrayList<>();
        List<Long> professoresIds = turmaDto.professoresIds() != null ? turmaDto.professoresIds() : new ArrayList<>();
        List<Long> alunosIds = turmaDto.alunosIds() != null ? turmaDto.alunosIds() : new ArrayList<>();

        // Atualiza disciplinas
        if (!disciplinasIds.isEmpty()) {
            Set<Disciplina> disciplinas = new HashSet<>(disciplinaRepository.findAllById(disciplinasIds));
            if (disciplinas.size() != disciplinasIds.size()) {
                throw new IllegalArgumentException("IDs de Disciplina inválidos. Algumas disciplinas não foram encontradas.");
            }
            turmaExistente.setDisciplinas(disciplinas);
        } else {
            turmaExistente.setDisciplinas(new HashSet<>());
        }

        // Atualiza professores
        if (!professoresIds.isEmpty()) {
            Set<Professor> professores = new HashSet<>(professorRepository.findAllById(professoresIds));
            if (professores.size() != professoresIds.size()) {
                throw new IllegalArgumentException("IDs de Professor inválidos. Alguns professores não foram encontrados.");
            }
            turmaExistente.setProfessores(professores);
        } else {
            turmaExistente.setProfessores(new HashSet<>());
        }

        // Atualiza alunos
        if (!alunosIds.isEmpty()) {
            Set<Aluno> alunos = new HashSet<>(alunoRepository.findAllById(alunosIds));
            if (alunos.size() != alunosIds.size()) {
                throw new IllegalArgumentException("IDs de Aluno inválidos. Alguns alunos não foram encontrados.");
            }
            turmaExistente.setAlunos(alunos);
        } else {
            turmaExistente.setAlunos(new HashSet<>());
        }

        Turma turmaAtualizada = turmaRepository.save(turmaExistente);
        logger.info("Turma atualizada com sucesso: {}", turmaAtualizada.getId());

        return toDto(turmaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        logger.info("Deletando turma com ID: {}", id);
        if (!turmaRepository.existsById(id)) {
            throw new NoSuchElementException("Turma não encontrada para exclusão com ID: " + id);
        }
        turmaRepository.deleteById(id);
        logger.info("Turma deletada com sucesso: {}", id);
    }

    @Transactional
    public void matricularAluno(Long turmaId, Long alunoId) {
        logger.info("Matriculando aluno {} na turma {}", alunoId, turmaId);

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new NoSuchElementException("Turma não encontrada com ID: " + turmaId));

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new NoSuchElementException("Aluno não encontrado com ID: " + alunoId));

        turma.getAlunos().add(aluno);
        turmaRepository.save(turma);
        logger.info("Aluno {} matriculado com sucesso na turma {}", alunoId, turmaId);
    }

    private TurmaDto toDto(Turma turma) {
        List<Long> disciplinasIds = turma.getDisciplinas().stream()
                .map(Disciplina::getId)
                .collect(Collectors.toList());

        List<Long> professoresIds = turma.getProfessores().stream()
                .map(Professor::getId)
                .collect(Collectors.toList());

        List<Long> alunosIds = turma.getAlunos().stream()
                .map(Aluno::getId)
                .collect(Collectors.toList());

        // Use cursoId se disponível, caso contrário use null
        Long cursoId = null; // Você precisará adicionar este campo na entidade Turma se necessário

        return new TurmaDto(turma.getId(), turma.getPeriodo(), cursoId, disciplinasIds, professoresIds, alunosIds);
    }
}