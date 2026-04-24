package com.pesapp.pesapp.entrenamientos.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaSesionEntrenamientoRequestDto {

    @NotBlank(message = "El nombre de la sesion es obligatorio")
    @Size(max = 150, message = "El nombre de la sesion no puede superar 150 caracteres")
    private String nombre;

    @Valid
    @NotEmpty(message = "La sesion debe tener al menos un ejercicio")
    private List<PlantillaEjercicioRequestDto> ejercicios = new ArrayList<>();
}
