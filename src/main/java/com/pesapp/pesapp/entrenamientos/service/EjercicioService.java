package com.pesapp.pesapp.entrenamientos.service;

import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioResponseDto;
import java.util.List;

public interface EjercicioService {

    List<EjercicioResponseDto> obtenerTodos();

    EjercicioResponseDto obtenerPorId(Long id);

    EjercicioResponseDto crear(EjercicioRequestDto request);

    EjercicioResponseDto actualizar(Long id, EjercicioRequestDto request);

    void eliminar(Long id);
}
