package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.service.HistoricoAcademicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/api/historicos")
public class HistoricoAcademicoController {

    private final HistoricoAcademicoService historicoService;

    public HistoricoAcademicoController(HistoricoAcademicoService historicoService) {
        this.historicoService = historicoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<HistoricoAcademicoDto> createHistorico(@Valid @RequestBody HistoricoAcademicoDto dto) {
        HistoricoAcademicoDto novoHistorico = historicoService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoHistorico);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<HistoricoAcademicoDto>> getAllHistoricos() {
        List<HistoricoAcademicoDto> historicos = historicoService.listarTodos();
        return ResponseEntity.ok(historicos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR', 'ALUNO')")
    public ResponseEntity<HistoricoAcademicoDto> getHistoricoById(@PathVariable Long id) {
        HistoricoAcademicoDto historico = historicoService.buscarPorId(id);
        return ResponseEntity.ok(historico);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<HistoricoAcademicoDto> updateHistorico(@PathVariable Long id, @Valid @RequestBody HistoricoAcademicoDto dto) {
        HistoricoAcademicoDto historicoAtualizado = historicoService.atualizar(id, dto);
        return ResponseEntity.ok(historicoAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHistorico(@PathVariable Long id) {
        historicoService.deletar(id);
    }
}