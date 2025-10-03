package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.TurmaDto;
import com.universidade.secretaria.mapper.TurmaMapper;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.model.Turma;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import com.universidade.secretaria.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    @Autowired private TurmaRepository turmaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private ProfessorRepository professorRepository;

    public Turma salvar(TurmaDto turmaDto) {
        List<Disciplina> disciplinas = disciplinaRepository.findAllById(turmaDto.disciplinasIds());


        if (disciplinas.size() != turmaDto.disciplinasIds().size()){
            throw new IllegalArgumentException("ID de Disciplina inválidos.");
        }

        Turma turma = TurmaMapper.toEntity(turmaDto, disciplinas);
        return turmaRepository.save(turma);
    }

    public List<Turma> listarTodos() {
        return turmaRepository.findAll();
    }

    public Optional<Turma> buscarPorId(Long id) {
        return turmaRepository.findById(id);
    }

    public Turma atualizar(Long id, TurmaDto turmaDto) {
        Turma turmaExistente = turmaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada para atualização."));

        List<Disciplina> disciplinas = disciplinaRepository.findAllById(turmaDto.disciplinasIds());


        if (disciplinas.size() != turmaDto.disciplinasIds().size()) {
            throw new IllegalArgumentException("IDs de Disciplina ou Professor inválidos.");
        }

        turmaExistente.setPeriodo(turmaDto.periodo());
        turmaExistente.setDisciplinas(disciplinas);


        return turmaRepository.save(turmaExistente);
    }

    public void deletar(Long id) {
        if (!turmaRepository.existsById(id)) {
            throw new IllegalArgumentException("Turma não encontrada para exclusão.");
        }
        turmaRepository.deleteById(id);
    }

    public void matricularAluno(Long turmaId, Long alunoId) {
        // ... (lógica do método matricularAluno, que foi movido para cá)
    }
}