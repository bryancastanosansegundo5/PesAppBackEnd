package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroSerieResponseDto {

    private Long id;
    private Long registroEjercicioId;
    private Integer numeroSerie;
    private Integer repeticiones;
    private BigDecimal peso;
    private Integer orden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
