package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaEjercicioResponseDto {

    private Long id;
    private Long plantillaSesionId;
    private String nombre;
    private String descripcion;
    private Integer seriesBase;
    private Integer repeticionesBase;
    private BigDecimal pesoBase;
    private BigDecimal alturaBanco;
    private String agarre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
