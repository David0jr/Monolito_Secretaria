package com.universidade.secretaria.service;

import com.universidade.secretaria.model.Usuario;
import com.universidade.secretaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Converte a lista de Perfis para uma lista de autoridades que o Spring Security entende
        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getPerfils().stream()
                        .map(perfil -> new SimpleGrantedAuthority(perfil.getNome().toString()))
                        .collect(Collectors.toList())
        );
    }
}

/*Este serviço, chamado UserDetailsServiceImpl, é quem vai pegar o nome de usuário que chega na requisição de login,
 buscar o usuário no banco de dados e fornecer ao Spring Security todos os dados necessários (username, senha e permissões)
 para a autenticação.*/
