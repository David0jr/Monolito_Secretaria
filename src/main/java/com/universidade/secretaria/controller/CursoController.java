package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.CursoDto;
import com.universidade.secretaria.model.Curso;
import com.universidade.secretaria.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@PreAuthorize("hasRole('SECRETARIA')")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @PostMapping
    public ResponseEntity<?> createCurso(@Valid @RequestBody CursoDto cursoDto) {
        try {
            Curso novoCurso = cursoService.salvar(cursoDto);
            return new ResponseEntity<>(novoCurso, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Curso>> getAllCursos() {
        List<Curso> cursos = cursoService.listarTodos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCursoById(@PathVariable Long id) {
        return cursoService.buscarPorId(id)
                .map(curso -> ResponseEntity.ok().body(curso))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurso(@PathVariable Long id, @Valid @RequestBody CursoDto cursoDto) {
        try {
            Curso cursoAtualizado = cursoService.atualizar(id, cursoDto);
            return ResponseEntity.ok(cursoAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCurso(@PathVariable Long id) {
        try {
            cursoService.deletar(id);
            return ResponseEntity.ok("Curso deletado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}