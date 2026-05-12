package com.pesapp.pesapp.entrenamientos.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantillaSesionEntrenamientoRequestDto {

    private String id;
    private String idSesion;
    private String clientId;

    @NotBlank(message = "El nombre de la sesion es obligatorio")
    @Size(max = 150, message = "El nombre de la sesion no puede superar 150 caracteres")
    private String nombreSesion;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres")
    private String observaciones;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private Long version;

    @Valid
    @NotEmpty(message = "La sesion debe tener al menos un ejercicio")
    private List<PlantillaEjercicioRequestDto> ejercicios = new ArrayList<>();
}
