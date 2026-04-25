package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaEjercicioResponseDto {

    private String idEjercicio;
    private String clientId;
    private String catalogoEjercicioId;
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
    private boolean completado;
    private boolean omitido;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<RegistroSerieResponseDto> seriesRealizadas = new ArrayList<>();
}
