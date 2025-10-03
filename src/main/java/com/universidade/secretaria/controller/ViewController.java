package com.universidade.secretaria.controller;

import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ViewController {

    @Autowired
    private UsuarioService usuarioService;


    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Principal principal, Model model) {
        String username = principal.getName();
        Optional<Usuario> usuario = usuarioService.findByUsername(username); // Chama o serviço para encontrar o usuário

        // Para simplificar, vamos passar apenas o username por enquanto
        model.addAttribute("username", username);

        return "dashboard";
    }
}