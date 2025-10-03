package com.universidade.secretaria.service;

import com.universidade.secretaria.dto.AlunoDto;
import com.universidade.secretaria.dto.VincularUsuarioDto;
import com.universidade.secretaria.exception.AlunoNaoEncontradoException;
import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.repository.AlunoRepository;
import com.universidade.secretaria.mapper.AlunoMapper;
import com.universidade.secretaria.model.Aluno;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Aluno salvar(AlunoDto alunoDto) {
        // Adicione a validação de CPF aqui
        if (alunoRepository.findByCpf(alunoDto.cpf()) != null) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        Aluno aluno = AlunoMapper.toEntity(alunoDto);
        return alunoRepository.save(aluno);
    }

    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    public Optional<Aluno> buscarPorId(Long id) {
        return alunoRepository.findById(id);
    }

    public Aluno atualizar(Long id, AlunoDto alunoDto) {
        Optional<Aluno> alunoOptional = alunoRepository.findById(id);
        if (alunoOptional.isEmpty()) {
            throw new AlunoNaoEncontradoException("Aluno não encontrado para atualização.");
        }
        Aluno alunoExistente = alunoOptional.get();
        // Atualiza apenas os campos do DTO na entidade existente
        alunoExistente.setNome(alunoDto.nome());
        alunoExistente.setCpf(alunoDto.cpf());
        return alunoRepository.save(alunoExistente);
    }

    public void deletar(Long id) {
        // Lógica de negócio: o AlunoService decide como deletar
        Optional<Aluno> alunoOptional = alunoRepository.findById(id);
        if (alunoOptional.isEmpty()) {
            throw new AlunoNaoEncontradoException("Aluno não encontrado para exclusão.");
        }
        alunoRepository.deleteById(id);
    }

    public void vincularUsuario(Long alunoId, VincularUsuarioDto vincularDto) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado."));

        Usuario usuario = usuarioRepository.findByUsername(vincularDto.username())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        aluno.setUsuario(usuario);
        alunoRepository.save(aluno);
    }
}