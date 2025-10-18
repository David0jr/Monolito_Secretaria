package com.universidade.secretaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller //  Correção: removido " new *"
public class PageController {

    //  PÁGINA INICIAL PÚBLICA - SEM AUTENTICAÇÃO
    @GetMapping("/") // Correção: removido "" e parâmetro incorreto
    public String home() {
        return "redirect:/login";
    }

    // PÁGINA DE LOGIN PÚBLICA
    @GetMapping("/login") // Correção: "/login" (minúsculo) e removido ""
    public String login() {
        return "login";
    }

    // DASHBOARDS PROTEGIDOS
    @GetMapping("/dashboard-alunos") // Correção: removido ""
    public String dashboardAlunos() {
        return "dashboard-aluno"; // CORRETO (sem 's' no final)
    }

    @GetMapping("/dashboard-professor") // Correção: removido ""
    public String dashboardProfessor() {
        return "dashboard-professor";
    }

    @GetMapping("/dashboard-secretaria") // Correção: removido ""
    public String dashboardSecretaria() {
        return "dashboard-secretaria";
    }

    // PÁGINA DE ACESSO NEGADO
    @GetMapping("/access-denied") // Correção: removido ""
    public String accessDenied() {
        return "access-denied"; // Correção: fechamento correto do método
    }
}