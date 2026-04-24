package com.pesapp.pesapp.usuarios.model.dto;

import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarRolUsuarioRequestDto {

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;
}
