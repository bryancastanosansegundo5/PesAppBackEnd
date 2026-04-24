package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtroEntrenoEntradaDto {

    private String id;
    private LocalDateTime fecha;
    private String nombreSesion;
    private Integer seriesPlanificadas;
    private Integer repeticionesPlanificadas;
    private BigDecimal pesoPlanificado;
    private String alturaBanco;
    private String agarre;
    private Integer seriesRealizadas;
    private Integer repeticionesTotales;
    private BigDecimal volumenTotal;
    private BigDecimal pesoMaximo;
}
