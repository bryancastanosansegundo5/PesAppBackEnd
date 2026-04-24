package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtroEntrenoChartPointDto {

    private LocalDateTime fecha;
    private BigDecimal valor;
}
