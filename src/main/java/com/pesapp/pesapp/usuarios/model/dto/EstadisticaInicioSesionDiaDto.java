package com.pesapp.pesapp.usuarios.model.dto;

import java.time.LocalDate;

public record EstadisticaInicioSesionDiaDto(
        LocalDate fecha,
        long iniciosSesion) {
}
