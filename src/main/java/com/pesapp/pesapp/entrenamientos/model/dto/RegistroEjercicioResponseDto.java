package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroEjercicioResponseDto {

    private Long id;
    private Long registroEntrenamientoId;
    private Long plantillaEjercicioId;
    private String nombre;
    private String descripcion;
    private Integer seriesBase;
    private Integer repeticionesBase;
    private BigDecimal pesoBase;
    private BigDecimal alturaBanco;
    private String agarre;
    private boolean omitido;
    private List<RegistroSerieResponseDto> seriesRealizadas = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
