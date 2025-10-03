package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.ProfessorDto;
import com.universidade.secretaria.model.Professor;
import com.universidade.secretaria.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    @Autowired
    private ProfessorService professorService;

    // Novo endpoint único para criar um professor e sua conta de usuário
    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<?> salvarProfessor(@Valid @RequestBody ProfessorDto professorDto) {
        try {
            Professor novoProfessor = professorService.salvar(professorDto);
            return new ResponseEntity<>(novoProfessor, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<Professor>> getAllProfessor() {
        return ResponseEntity.status(HttpStatus.OK).body(professorService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')")
    public ResponseEntity<?> getProfessorId(@PathVariable(value = "id") Long id) {
        Optional<Professor> professor = professorService.buscarPorId(id);
        if (professor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Professor de id " + id + " não foi encontrado.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(professor.get());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<?> atualizarProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorDto professorDto) {
        try {
            Professor professorAtualizado = professorService.atualizar(id, professorDto);
            return ResponseEntity.ok(professorAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<?> DeletarProfessor(@PathVariable(value = "id") Long id) {
        try {
            professorService.deletar(id);
            return ResponseEntity.status(HttpStatus.OK).body("Professor id " + id + " removido com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}