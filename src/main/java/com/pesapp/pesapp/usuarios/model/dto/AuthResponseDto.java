package com.pesapp.pesapp.usuarios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {

    private String tokenType;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;
    private boolean authenticated;
    private UsuarioResponseDto usuario;
}
