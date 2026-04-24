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
    private String idSesion;
    private String nombreSesion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private List<RegistroEjercicioResponseDto> ejercicios = new ArrayList<>();
}
