package com.pesapp.pesapp.usuarios.model.dto;

import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResponseDto {

    private Long id;
    private String nombre;
    private String username;
    private String email;
    private RolUsuario rol;
    private boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
