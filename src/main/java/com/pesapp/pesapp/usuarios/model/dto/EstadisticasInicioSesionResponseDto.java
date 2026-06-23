package com.pesapp.pesapp.usuarios.model.dto;

import java.util.List;

public record EstadisticasInicioSesionResponseDto(
        List<EstadisticaInicioSesionDiaDto> dias,
        long total,
        long usuariosUnicos) {
}
