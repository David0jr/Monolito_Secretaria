package com.universidade.secretaria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String expectedRole = (String) request.getAttribute("expectedRole");
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Se foi especificado um tipo de usuário, validar se corresponde
        if (expectedRole != null) {
            boolean hasExpectedRole = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals(expectedRole));

            if (!hasExpectedRole) {
                response.sendRedirect("/login?error=true&message=wrong_role");
                return;
            }
        }

        // Redirecionamento baseado no role
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA"))) {
            response.sendRedirect("/dashboard-secretaria");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR"))) {
            response.sendRedirect("/dashboard-professor");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO"))) {
            response.sendRedirect("/dashboard-aluno");
        } else {
            response.sendRedirect("/home");
        }
    }
}