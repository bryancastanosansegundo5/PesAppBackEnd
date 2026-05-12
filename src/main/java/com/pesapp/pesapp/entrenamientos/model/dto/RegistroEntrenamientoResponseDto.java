package com.pesapp.pesapp.entrenamientos.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroEntrenamientoResponseDto {

    private String id;
    private String persistedId;
    private String idSesion;
    private String clientId;
    private String nombreSesion;
    private String observaciones;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<RegistroEjercicioResponseDto> ejercicios = new ArrayList<>();
}
