package com.pesapp.pesapp.usuarios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogoutResponseDto {

    private boolean logout;
    private String mensaje;
}
