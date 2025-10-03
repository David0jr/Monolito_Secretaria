package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.model.Disciplina;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.repository.DisciplinaRepository;
import com.universidade.secretaria.repository.ProfessorRepository;
import com.universidade.secretaria.service.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/disciplinas")
@PreAuthorize("hasRole('SECRETARIA')")
public class DisciplinaController {

    @Autowired
    private DisciplinaService disciplinaService;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @PostMapping
    public ResponseEntity<?> createDisciplina(@Valid @RequestBody DisciplinaDto disciplinaDto) {
        try {
            Disciplina novaDisciplina = disciplinaService.salvar(disciplinaDto);
            return new ResponseEntity<>(novaDisciplina, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{disciplinaId}/associar-professor")
    public ResponseEntity<String> associarProfessor(@PathVariable Long disciplinaId, @RequestBody Long professorId) {
        try {
            disciplinaService.associarProfessor(disciplinaId, professorId);
            return ResponseEntity.ok("Professor associado à disciplina com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Disciplina>> getAllDisciplinas() {
        List<Disciplina> disciplinas = disciplinaService.listarTodos();
        return ResponseEntity.ok(disciplinas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDisciplinaById(@PathVariable Long id) {
        Optional<Disciplina> disciplinaOpt = disciplinaService.buscarPorId(id);
        if (disciplinaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Disciplina não encontrada.");
        }
        return ResponseEntity.ok(disciplinaOpt.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDisciplina(@PathVariable Long id, @Valid @RequestBody DisciplinaDto disciplinaDto) {
        try {
            Disciplina disciplinaAtualizada = disciplinaService.atualizar(id, disciplinaDto);
            return ResponseEntity.ok(disciplinaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDisciplina(@PathVariable Long id) {
        try {
            disciplinaService.deletar(id);
            return ResponseEntity.ok("Disciplina deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}