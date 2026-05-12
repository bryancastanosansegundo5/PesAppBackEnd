package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroSerieResponseDto {

    private String id;
    private String persistedId;
    private Integer numeroSerie;
    private Integer repeticiones;
    private BigDecimal peso;
    private String clientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
