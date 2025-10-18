package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.CursoDto;
import com.universidade.secretaria.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/cursos")
@PreAuthorize("hasRole('SECRETARIA')") // Apenas SECRETARIA pode gerenciar cursos
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<CursoDto> createCurso(@Valid @RequestBody CursoDto cursoDto) {
        CursoDto novoCurso = cursoService.salvar(cursoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCurso);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')") // Todos podem listar cursos
    public ResponseEntity<List<CursoDto>> getAllCursos() {
        List<CursoDto> cursos = cursoService.listarTodos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')") // Todos podem buscar curso por ID
    public ResponseEntity<CursoDto> getCursoById(@PathVariable Long id) {
        CursoDto curso = cursoService.buscarPorId(id);
        return ResponseEntity.ok(curso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoDto> updateCurso(@PathVariable Long id, @Valid @RequestBody CursoDto cursoDto) {
        CursoDto cursoAtualizado = cursoService.atualizar(id, cursoDto);
        return ResponseEntity.ok(cursoAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurso(@PathVariable Long id) {
        cursoService.deletar(id);
    }
}