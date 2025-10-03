package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.AlunoDto;
import com.universidade.secretaria.dto.VincularUsuarioDto;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.service.AlunoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
@PreAuthorize("hasRole('SECRETARIA')")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @PostMapping
    public ResponseEntity<Aluno> salvarAluno(@RequestBody AlunoDto alunoDto) {
        try {
            Aluno novoAluno = alunoService.salvar(alunoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{alunoId}/vincular-usuario")//Vincula o Usuario a um aluno
    public ResponseEntity<?> vincularUsuario(@PathVariable Long alunoId, @RequestBody VincularUsuarioDto vincularDto) {
        try {
            alunoService.vincularUsuario(alunoId, vincularDto);
            return ResponseEntity.ok("Usuário vinculado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Aluno>> getAllAluno() {
        return ResponseEntity.status(HttpStatus.OK).body(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAlunoId(@PathVariable(value = "id") Long id) {
        Optional<Aluno> aluno = alunoService.buscarPorId(id);
        if (aluno.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno de id " + id + " não foi encontrado.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(aluno.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarAluno(@PathVariable Long id, @RequestBody AlunoDto alunoDto) {
        try {
            Aluno alunoAtualizado = alunoService.atualizar(id, alunoDto);
            return ResponseEntity.ok(alunoAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> DeletarAluno(@PathVariable(value = "id") Long id) {
        try {
            alunoService.deletar(id);
            return ResponseEntity.status(HttpStatus.OK).body("Aluno id " + id + " removido com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}