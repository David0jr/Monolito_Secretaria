package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.model.HistoricoAcademico;
import com.universidade.secretaria.service.HistoricoAcademicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/historicos")
public class HistoricoAcademicoController {

    @Autowired
    private HistoricoAcademicoService historicoService;

    // Apenas Secretaria e Professor podem gerenciar históricos
    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<?> createHistorico(@Valid @RequestBody HistoricoAcademicoDto dto) {
        try {
            HistoricoAcademico novoHistorico = historicoService.salvar(dto);
            return new ResponseEntity<>(novoHistorico, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Apenas Secretaria e Professor podem listar todos
    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<HistoricoAcademico>> getAllHistoricos() {
        List<HistoricoAcademico> historicos = historicoService.listarTodos();
        return ResponseEntity.ok(historicos);
    }

    // Apenas Secretaria e Professor podem atualizar
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<?> updateHistorico(@PathVariable Long id, @Valid @RequestBody HistoricoAcademicoDto dto) {
        try {
            HistoricoAcademico historicoAtualizado = historicoService.atualizar(id, dto);
            return ResponseEntity.ok(historicoAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Apenas Secretaria e Professor podem deletar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<?> deleteHistorico(@PathVariable Long id) {
        try {
            historicoService.deletar(id);
            return ResponseEntity.ok("Histórico acadêmico deletado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}