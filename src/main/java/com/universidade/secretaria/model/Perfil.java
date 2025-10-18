package com.universidade.secretaria.model;

import com.universidade.secretaria.enums.PerfilEnum;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name= "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private PerfilEnum nome;

    public Perfil() {
    }

    public Perfil(long id, PerfilEnum nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PerfilEnum getNome() {
        return nome;
    }

    public void setNome(PerfilEnum nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Perfil perfil = (Perfil) o;
        return id == perfil.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}