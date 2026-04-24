package com.pesapp.pesapp.usuarios.model.dto;

import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearUsuarioAdminRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @Email(message = "El email no tiene un formato valido")
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 180, message = "El email no puede superar 180 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "La contrasena debe incluir al menos una letra y un numero")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;
}
