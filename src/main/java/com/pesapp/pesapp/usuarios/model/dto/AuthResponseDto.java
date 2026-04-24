package com.pesapp.pesapp.usuarios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UsuarioResponseDto usuario;
}
