package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.service.DisciplinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<DisciplinaDto> createDisciplina(@Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto novaDisciplina = disciplinaService.salvar(disciplinaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaDisciplina);
    }

    // Endpoint para associar um único professor a uma disciplina
    // O RequestBody agora espera um Long (ID do professor)
    @PostMapping("/{disciplinaId}/associar-professor") // Voltou a ser singular no nome do endpoint
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<DisciplinaDto> associarProfessor(@PathVariable Long disciplinaId, @RequestBody Long professorId) {
        DisciplinaDto disciplinaAtualizada = disciplinaService.associarProfessor(disciplinaId, professorId);
        return ResponseEntity.ok(disciplinaAtualizada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')")
    public ResponseEntity<List<DisciplinaDto>> getAllDisciplinas() {
        List<DisciplinaDto> disciplinas = disciplinaService.listarTodos();
        return ResponseEntity.ok(disciplinas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')")
    public ResponseEntity<DisciplinaDto> getDisciplinaById(@PathVariable Long id) {
        DisciplinaDto disciplina = disciplinaService.buscarPorId(id);
        return ResponseEntity.ok(disciplina);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<DisciplinaDto> updateDisciplina(@PathVariable Long id, @Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto disciplinaAtualizada = disciplinaService.atualizar(id, disciplinaDto);
        return ResponseEntity.ok(disciplinaAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDisciplina(@PathVariable Long id) {
        disciplinaService.deletar(id);
    }
}