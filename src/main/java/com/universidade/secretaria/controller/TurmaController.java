package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.TurmaDto;
import com.universidade.secretaria.model.Turma;
import com.universidade.secretaria.service.TurmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turmas")
@PreAuthorize("hasRole('SECRETARIA')")
public class TurmaController {

    @Autowired private TurmaService turmaService;

    @PostMapping
    public ResponseEntity<?> createTurma(@Valid @RequestBody TurmaDto turmaDto) {
        try {
            Turma novaTurma = turmaService.salvar(turmaDto);
            return new ResponseEntity<>(novaTurma, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Turma>> getAllTurmas() {
        List<Turma> turmas = turmaService.listarTodos();
        return ResponseEntity.ok(turmas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTurmaById(@PathVariable Long id) {
        Optional<Turma> turma = turmaService.buscarPorId(id);
        if (turma.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Turma não encontrada.");
        }
        return ResponseEntity.ok(turma.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTurma(@PathVariable Long id, @Valid @RequestBody TurmaDto turmaDto) {
        try {
            Turma turmaAtualizada = turmaService.atualizar(id, turmaDto);
            return ResponseEntity.ok(turmaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTurma(@PathVariable Long id) {
        try {
            turmaService.deletar(id);
            return ResponseEntity.ok("Turma deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{turmaId}/matricular-aluno")
    public ResponseEntity<?> matricularAluno(@PathVariable Long turmaId, @RequestBody Long alunoId) {
        try {
            turmaService.matricularAluno(turmaId, alunoId);
            return ResponseEntity.ok("Aluno matriculado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}