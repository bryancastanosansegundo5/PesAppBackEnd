package com.pesapp.pesapp.usuarios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthSessionDto {

    private String accessToken;
    private String refreshToken;
    private AuthResponseDto response;
}
