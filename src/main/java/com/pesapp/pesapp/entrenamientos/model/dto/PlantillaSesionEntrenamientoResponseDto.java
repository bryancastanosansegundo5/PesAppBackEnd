package com.pesapp.pesapp.entrenamientos.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaSesionEntrenamientoResponseDto {

    private Long id;
    private String nombre;
    private List<PlantillaEjercicioResponseDto> ejercicios = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
