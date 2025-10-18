package com.universidade.secretaria.dto;

import com.universidade.secretaria.enums.PerfilEnum;
import jakarta.validation.constraints.NotBlank;

public record UsuarioDto(
        long id, @NotBlank(message = "O nome de usuário é obrigatório")
        String username,

        @NotBlank(message = "A senha é obrigatória")
        String password,

        PerfilEnum perfil
) {}