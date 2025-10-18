package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.HistoricoAcademico;
import com.universidade.secretaria.repository.AlunoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.HistoricoAcademicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class HistoricoAcademicoService {

    private final HistoricoAcademicoRepository historicoRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public HistoricoAcademicoService(HistoricoAcademicoRepository historicoRepository, AlunoRepository alunoRepository, DisciplinaRepository disciplinaRepository) {
        this.historicoRepository = historicoRepository;
        this.alunoRepository = alunoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional
    public HistoricoAcademicoDto salvar(HistoricoAcademicoDto dto) {
        // Busca as entidades relacionadas, garantindo que existam
        Aluno aluno = alunoRepository.findById(dto.alunoId())
                .orElseThrow(() -> new NoSuchElementException("Aluno não encontrado."));
        Disciplina disciplina = disciplinaRepository.findById(dto.disciplinaId())
                .orElseThrow(() -> new NoSuchElementException("Disciplina não encontrada."));

        // Lógica de negócio: verifica se já existe um histórico para o aluno e a disciplina
        historicoRepository.findByAlunoAndDisciplina(aluno, disciplina).ifPresent(h -> {
            throw new IllegalArgumentException("Já existe um histórico para este aluno nesta disciplina.");
        });

        HistoricoAcademico historico = new HistoricoAcademico();
        historico.setNota(dto.nota());
        historico.setFrequencia(dto.frequencia());
        historico.setAluno(aluno);
        historico.setDisciplina(disciplina);
        historico.setDataRegistro(LocalDateTime.now());
        historico.setStatus(calcularStatus(dto.nota(), dto.frequencia()));

        HistoricoAcademico historicoSalvo = historicoRepository.save(historico);
        return toDto(historicoSalvo);
    }

    public List<HistoricoAcademicoDto> listarTodos() {
        return historicoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public HistoricoAcademicoDto buscarPorId(Long id) {
        HistoricoAcademico historico = historicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Histórico acadêmico não encontrado."));
        return toDto(historico);
    }

    @Transactional
    public HistoricoAcademicoDto atualizar(Long id, HistoricoAcademicoDto dto) {
        HistoricoAcademico historicoExistente = historicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Histórico acadêmico não encontrado."));

        // As entidades Aluno e Disciplina não devem ser alteradas na atualização de um histórico.
        // Se a nota ou frequência forem atualizadas, o status também deve ser recalculado.
        historicoExistente.setNota(dto.nota());
        historicoExistente.setFrequencia(dto.frequencia());
        historicoExistente.setStatus(calcularStatus(dto.nota(), dto.frequencia()));

        HistoricoAcademico historicoAtualizado = historicoRepository.save(historicoExistente);
        return toDto(historicoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        historicoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Histórico acadêmico não encontrado."));
        historicoRepository.deleteById(id);
    }

    // Método utilitário para converter entidade para DTO
    private HistoricoAcademicoDto toDto(HistoricoAcademico historico) {
        return new HistoricoAcademicoDto(
                historico.getId(),
                historico.getNota(),
                historico.getFrequencia(),
                historico.getAluno().getId(),
                historico.getDisciplina().getId()
        );
    }

    // Lógica de negócio para calcular o status
    private String calcularStatus(double nota, double frequencia) {
        if (frequencia < 75) {
            return "Reprovado por Falta";
        }
        if (nota >= 7.0) {
            return "Aprovado";
        }
        if (nota >= 5.0) {
            return "Recuperação";
        }
        return "Reprovado";
    }
}