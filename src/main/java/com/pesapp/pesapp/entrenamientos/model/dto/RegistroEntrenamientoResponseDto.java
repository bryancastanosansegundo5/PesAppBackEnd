package com.pesapp.pesapp.entrenamientos.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroEntrenamientoResponseDto {

    private Long id;
    private Long plantillaSesionId;
    private Long usuarioId;
    private String nombreSesion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
    private List<RegistroEjercicioResponseDto> ejercicios = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
