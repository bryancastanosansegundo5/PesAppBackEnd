package com.pesapp.pesapp.usuarios.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarEstadoUsuarioRequestDto {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
