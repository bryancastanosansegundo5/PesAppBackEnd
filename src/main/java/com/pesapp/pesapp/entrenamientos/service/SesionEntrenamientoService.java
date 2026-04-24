package com.pesapp.pesapp.entrenamientos.service;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import java.util.List;

public interface SesionEntrenamientoService {

    List<PlantillaSesionEntrenamientoResponseDto> obtenerTodas();

    PlantillaSesionEntrenamientoResponseDto obtenerPorId(Long id);

    PlantillaSesionEntrenamientoResponseDto crear(PlantillaSesionEntrenamientoRequestDto request);

    PlantillaSesionEntrenamientoResponseDto actualizar(Long id, PlantillaSesionEntrenamientoRequestDto request);

    void eliminar(Long id);
}
