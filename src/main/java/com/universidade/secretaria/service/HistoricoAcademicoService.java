package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.mapper.HistoricoAcademicoMapper;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.HistoricoAcademico;
import com.universidade.secretaria.repository.AlunoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.HistoricoAcademicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoricoAcademicoService {

    @Autowired
    private HistoricoAcademicoRepository historicoRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private DisciplinaRepository disciplinaRepository;

    public HistoricoAcademico salvar(HistoricoAcademicoDto dto) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(dto.alunoId());
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(dto.disciplinaId());

        if (alunoOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            throw new IllegalArgumentException("Aluno ou Disciplina não encontrados.");
        }

        HistoricoAcademico historico = HistoricoAcademicoMapper.toEntity(dto, alunoOpt.get(), disciplinaOpt.get());
        return historicoRepository.save(historico);
    }

    public List<HistoricoAcademico> listarTodos() {
        return historicoRepository.findAll();
    }

    public Optional<HistoricoAcademico> buscarPorId(Long id) {
        return historicoRepository.findById(id);
    }

    public HistoricoAcademico atualizar(Long id, HistoricoAcademicoDto dto) {
        HistoricoAcademico historicoExistente = historicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Histórico acadêmico não encontrado."));

        Optional<Aluno> alunoOpt = alunoRepository.findById(dto.alunoId());
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(dto.disciplinaId());

        if (alunoOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            throw new IllegalArgumentException("Aluno ou Disciplina não encontrados.");
        }

        historicoExistente.setNota(dto.nota());
        historicoExistente.setFrequencia(dto.frequencia());
        historicoExistente.setAluno(alunoOpt.get());
        historicoExistente.setDisciplina(disciplinaOpt.get());

        return historicoRepository.save(historicoExistente);
    }

    public void deletar(Long id) {
        if (!historicoRepository.existsById(id)) {
            throw new IllegalArgumentException("Histórico acadêmico não encontrado.");
        }
        historicoRepository.deleteById(id);
    }
}