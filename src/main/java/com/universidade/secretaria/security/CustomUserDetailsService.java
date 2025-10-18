package com.universidade.secretaria.security;

import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.model.Perfil;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        System.out.println("=== DEBUG: Carregando usuário ===");
        System.out.println("Username: " + usuario.getUsername());
        System.out.println("Perfis: " + usuario.getPerfis().stream()
                .map(p -> p.getNome().name())
                .collect(Collectors.toList()));
        System.out.println("===============================");

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                mapPerfisToAuthorities(usuario.getPerfis())
        );
    }

    private Collection<? extends GrantedAuthority> mapPerfisToAuthorities(Collection<Perfil> perfis) {
        return perfis.stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.getNome().name()))
                .collect(Collectors.toList());
    }
}