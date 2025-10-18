package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.DisciplinaDto;
import com.universidade.secretaria.dto.HistoricoAcademicoDto;
import com.universidade.secretaria.dto.ProfessorRegistroDTO;
import com.universidade.secretaria.service.ProfessorService;
import com.universidade.secretaria.service.HistoricoAcademicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService professorService;
    private final HistoricoAcademicoService historicoAcademicoService;

    public ProfessorController(ProfessorService professorService, HistoricoAcademicoService historicoAcademicoService) {
        this.professorService = professorService;
        this.historicoAcademicoService = historicoAcademicoService;
    }

    // POST - Criação
    @PostMapping
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<ProfessorRegistroDTO> salvarProfessor(@Valid @RequestBody ProfessorRegistroDTO professorRegistroDTO) {
        ProfessorRegistroDTO novoProfessor = professorService.salvar(professorRegistroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProfessor);
    }

    // GET - Consulta (agora com ID!)
    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<ProfessorRegistroDTO>> getAllProfessores() {
        List<ProfessorRegistroDTO> professores = professorService.listarTodos();
        return ResponseEntity.status(HttpStatus.OK).body(professores);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARIA') or @securityService.isUserOfId(#id)")
    public ResponseEntity<ProfessorRegistroDTO> getProfessorById(@PathVariable Long id) {
        ProfessorRegistroDTO professor = professorService.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(professor);
    }

    @GetMapping("/{id}/disciplinas")
    public ResponseEntity<Set<DisciplinaDto>> getDisciplinasByProfessorId(@PathVariable Long id) {
        Set<DisciplinaDto> disciplinas = professorService.getDisciplinasByProfessorId(id);
        return ResponseEntity.ok(disciplinas);
    }

    @PostMapping("/{professorId}/disciplinas/{disciplinaId}/lancar-notas")
    @PreAuthorize("hasAnyRole('SECRETARIA') or @securityService.isUserOfId(#professorId)")
    public ResponseEntity<HistoricoAcademicoDto> lancarNotas(
            @PathVariable Long professorId,
            @PathVariable Long disciplinaId,
            @Valid @RequestBody HistoricoAcademicoDto historicoAcademicoDto
    ) {
        HistoricoAcademicoDto novoHistorico = historicoAcademicoService.salvar(historicoAcademicoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoHistorico);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    public ResponseEntity<ProfessorRegistroDTO> atualizarProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorRegistroDTO professorDto) {
        ProfessorRegistroDTO professorAtualizado = professorService.atualizar(id, professorDto);
        return ResponseEntity.ok(professorAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARIA')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarProfessor(@PathVariable Long id) {
        professorService.deletar(id);
    }
}