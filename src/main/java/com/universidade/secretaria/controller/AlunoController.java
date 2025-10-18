package com.universidade.secretaria.controller;

import com.universidade.secretaria.dto.AlunoCadastroDto;
import com.universidade.secretaria.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<AlunoCadastroDto> alunos = alunoService.listarTodos();
            return ResponseEntity.ok(alunos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar alunos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            AlunoCadastroDto aluno = alunoService.buscarPorId(id);
            return ResponseEntity.ok(aluno);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erro ao buscar aluno: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(@Valid @RequestBody AlunoCadastroDto alunoCadastroDto) {
        try {
            System.out.println("Recebendo requisição para cadastrar aluno: " + alunoCadastroDto.username());
            AlunoCadastroDto alunoSalvo = alunoService.salvar(alunoCadastroDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(alunoSalvo);
        } catch (Exception e) {
            System.err.println("ERRO no controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar aluno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoCadastroDto alunoCadastroDto) {
        try {
            AlunoCadastroDto alunoAtualizado = alunoService.atualizar(id, alunoCadastroDto);
            return ResponseEntity.ok(alunoAtualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar aluno: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            alunoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar aluno: " + e.getMessage());
        }
    }
}