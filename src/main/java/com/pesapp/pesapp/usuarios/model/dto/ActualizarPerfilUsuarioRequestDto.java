package com.pesapp.pesapp.usuarios.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarPerfilUsuarioRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 60, message = "El nombre de usuario debe tener entre 3 y 60 caracteres")
    @Pattern(
            regexp = "^[A-Za-z0-9._-]+$",
            message = "El nombre de usuario solo puede contener letras, numeros, puntos, guiones y guion bajo")
    private String username;

    @Size(max = 180, message = "El email no puede superar 180 caracteres")
    private String email;
}
