package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroEjercicioResponseDto {

    private String idEjercicio;
    private String nombre;
    private Integer seriesPlanificadas;
    private Integer repeticionesPlanificadas;
    private BigDecimal pesoPlanificado;
    private BigDecimal alturaBanco;
    private String agarre;
    private boolean completado;
    private boolean omitido;
    private List<RegistroSerieResponseDto> seriesRealizadas = new ArrayList<>();
}
