package com.universidade.secretaria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "disciplinas")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int cargaHoraria;

    // Relacionamento ManyToMany com Curso (uma disciplina pode estar em vários cursos)
    @ManyToMany(mappedBy = "disciplinas", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Curso> cursos = new HashSet<>();

    // Relacionamento ManyToMany com Turma (uma disciplina pode estar em várias turmas)
    @ManyToMany(mappedBy = "disciplinas", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Turma> turmas = new HashSet<>();

    // Relacionamento ManyToOne com Professor (uma disciplina tem um professor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    // Relacionamento ManyToMany para pré-requisitos (uma disciplina pode ter vários pré-requisitos e ser pré-requisito de várias)
    @ManyToMany
    @JoinTable(
            name = "disciplina_pre_requisito",
            joinColumns = @JoinColumn(name = "disciplina_id"),
            inverseJoinColumns = @JoinColumn(name = "pre_requisito_id"))
    private Set<Disciplina> preRequisitos = new HashSet<>();

    public Disciplina() {
    }

    public Disciplina(Long id, int cargaHoraria, String nome, Set<Curso> cursos, Set<Disciplina> preRequisitos) {
        this.id = id;
        this.cargaHoraria = cargaHoraria;
        this.nome = nome;
        this.cursos = cursos; // Inicializa o Set de cursos
        this.preRequisitos = preRequisitos;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public Set<Curso> getCursos() { // Retorna um Set de Cursos
        return cursos;
    }

    public void setCursos(Set<Curso> cursos) { // Define um Set de Cursos
        this.cursos = cursos;
    }

    public Set<Turma> getTurmas() {
        return turmas;
    }

    public void setTurmas(Set<Turma> turmas) {
        this.turmas = turmas;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public Set<Disciplina> getPreRequisitos() {
        return preRequisitos;
    }

    public void setPreRequisitos(Set<Disciplina> preRequisitos) {
        this.preRequisitos = preRequisitos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disciplina that = (Disciplina) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}