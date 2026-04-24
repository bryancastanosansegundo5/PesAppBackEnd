package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroSerieResponseDto {

    private String id;
    private Integer numeroSerie;
    private Integer repeticiones;
    private BigDecimal peso;
}
