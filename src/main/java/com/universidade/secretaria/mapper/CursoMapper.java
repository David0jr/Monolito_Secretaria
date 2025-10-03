package com.universidade.secretaria.mapper;

import com.universidade.secretaria.dto.CursoDto;
import com.universidade.secretaria.model.Curso;

public class CursoMapper {

    public static Curso toEntity(CursoDto dto) {
        if (dto == null) {
            return null;
        }
        Curso curso = new Curso();
        curso.setNome(dto.nome());
        curso.setDescricao(dto.descricao());
        return curso;
    }

    public static CursoDto toDto(Curso entidade) {
        if (entidade == null) {
            return null;
        }
        return new CursoDto(entidade.getNome(), entidade.getDescricao());
    }
}