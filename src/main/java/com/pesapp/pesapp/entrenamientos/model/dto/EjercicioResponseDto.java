package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EjercicioResponseDto {

    private String id;
    private String idEjercicio;
    private String clientId;
    private String nombre;
    private String descripcion;
    private String grupoMuscular;
    private String patronMovimiento;
    private String equipamiento;
    private Integer seriesPlanificadas;
    private Integer repeticionesPlanificadas;
    private BigDecimal pesoPlanificado;
    private String alturaBanco;
    private String agarre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
