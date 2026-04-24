package com.pesapp.pesapp.entrenamientos.service;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import java.util.List;
import java.util.Optional;

public interface EntrenamientoService {

    RegistroEntrenamientoResponseDto guardarEntrenamientoFinalizado(RegistroEntrenamientoRequestDto request);

    List<RegistroEntrenamientoResponseDto> obtenerHistorico();

    RegistroEntrenamientoResponseDto obtenerPorId(Long id);

    Optional<RegistroEjercicioResponseDto> obtenerUltimoRegistroEjercicio(Long plantillaEjercicioId);

    Optional<RegistroEntrenamientoResponseDto> obtenerUltimoRegistroSesion(Long plantillaSesionId);
}
