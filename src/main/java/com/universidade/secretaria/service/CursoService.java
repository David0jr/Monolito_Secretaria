package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.CursoDto;
import com.universidade.secretaria.mapper.CursoMapper;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public Curso salvar(CursoDto cursoDto) {
        if (cursoRepository.findByNome(cursoDto.nome()).isPresent()) {
            throw new IllegalArgumentException("Já existe um curso com este nome.");
        }
        Curso curso = CursoMapper.toEntity(cursoDto);
        return cursoRepository.save(curso);
    }

    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepository.findById(id);
    }

    public Curso atualizar(Long id, CursoDto cursoDto) {
        Curso cursoExistente = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado."));

        cursoExistente.setNome(cursoDto.nome());
        cursoExistente.setDescricao(cursoDto.descricao());

        return cursoRepository.save(cursoExistente);
    }

    public void deletar(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new IllegalArgumentException("Curso não encontrado.");
        }
        cursoRepository.deleteById(id);
    }
}