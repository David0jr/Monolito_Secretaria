package com.universidade.secretaria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//Validar as credencias com as Roles no html de login.
@Component
public class RoleValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("/perform_login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String userType = request.getParameter("userType");
            String username = request.getParameter("username");

            if (userType != null && username != null) {
                request.setAttribute("expectedRole", "ROLE_" + userType);
            }
        }

        filterChain.doFilter(request, response);
    }
}