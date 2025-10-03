package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.mapper.DisciplinaMapper;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.repository.CursoRepository;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisciplinaService {

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired // Injete o repositório do Professor
    private ProfessorRepository professorRepository;

    public Disciplina salvar(DisciplinaDto disciplinaDto) {
        Optional<Curso> cursoOpt = cursoRepository.findById(disciplinaDto.cursoId());
        if (cursoOpt.isEmpty()) {
            throw new IllegalArgumentException("Curso não encontrado com o ID fornecido.");
        }

        List<Disciplina> preRequisitos = disciplinaRepository.findAllById(disciplinaDto.preRequisitosIds());

        Disciplina disciplina = DisciplinaMapper.toEntity(disciplinaDto, cursoOpt.get(), preRequisitos);
        return disciplinaRepository.save(disciplina);
    }

    // --- NOVO MÉTODO ADICIONADO PARA A ASSOCIAÇÃO ---
    public void associarProfessor(Long disciplinaId, Long professorId) {
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada."));
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));

        disciplina.getProfessores().add(professor);
        disciplinaRepository.save(disciplina);
    }
    // --- FIM DO NOVO MÉTODO ---

    public List<Disciplina> listarTodos() {
        return disciplinaRepository.findAll();
    }

    public Optional<Disciplina> buscarPorId(Long id) {
        return disciplinaRepository.findById(id);
    }

    public Disciplina atualizar(Long id, DisciplinaDto disciplinaDto) {
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(id);
        if (disciplinaOpt.isEmpty()) {
            throw new IllegalArgumentException("Disciplina não encontrada para atualização.");
        }

        Optional<Curso> cursoOpt = cursoRepository.findById(disciplinaDto.cursoId());
        if (cursoOpt.isEmpty()) {
            throw new IllegalArgumentException("Curso não encontrado com o ID fornecido.");
        }

        Disciplina disciplinaExistente = disciplinaOpt.get();

        disciplinaExistente.setNome(disciplinaDto.nome());
        disciplinaExistente.setCargaHoraria(disciplinaDto.cargaHoraria());
        disciplinaExistente.setCurso(cursoOpt.get());

        List<Disciplina> novosPreRequisitos = disciplinaRepository.findAllById(disciplinaDto.preRequisitosIds());
        disciplinaExistente.setPreRequisitos(novosPreRequisitos);

        return disciplinaRepository.save(disciplinaExistente);
    }

    public void deletar(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new IllegalArgumentException("Disciplina não encontrada para exclusão.");
        }
        disciplinaRepository.deleteById(id);
    }
}