package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.TurmaDto;
import com.universidade.secretaria.service.TurmaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/turmas")
@PreAuthorize("hasRole('SECRETARIA')")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PostMapping
    public ResponseEntity<TurmaDto> createTurma(@Valid @RequestBody TurmaDto turmaDto) {
        TurmaDto novaTurma = turmaService.salvar(turmaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaTurma);
    }

    @GetMapping
    public ResponseEntity<List<TurmaDto>> getAllTurmas() {
        List<TurmaDto> turmas = turmaService.listarTodos();
        return ResponseEntity.ok(turmas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDto> getTurmaById(@PathVariable Long id) {
        TurmaDto turma = turmaService.buscarPorId(id);
        return ResponseEntity.ok(turma);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaDto> updateTurma(@PathVariable Long id, @Valid @RequestBody TurmaDto turmaDto) {
        TurmaDto turmaAtualizada = turmaService.atualizar(id, turmaDto);
        return ResponseEntity.ok(turmaAtualizada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTurma(@PathVariable Long id) {
        turmaService.deletar(id);
    }

    @PostMapping("/{turmaId}/matricular-aluno")
    public ResponseEntity<String> matricularAluno(@PathVariable Long turmaId, @RequestBody Long alunoId) {
        turmaService.matricularAluno(turmaId, alunoId);
        return ResponseEntity.ok("Aluno matriculado com sucesso!");
    }
}