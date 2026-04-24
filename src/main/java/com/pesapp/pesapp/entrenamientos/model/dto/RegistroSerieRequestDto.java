package com.pesapp.pesapp.entrenamientos.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroSerieRequestDto {

    @NotNull(message = "El numero de serie es obligatorio")
    @Min(value = 1, message = "El numero de serie debe ser mayor que cero")
    private Integer numeroSerie;

    @NotNull(message = "Las repeticiones son obligatorias")
    @Min(value = 0, message = "Las repeticiones no pueden ser negativas")
    private Integer repeticiones;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso no puede ser negativo")
    private BigDecimal peso;

    @NotNull(message = "El orden es obligatorio")
    @Min(value = 1, message = "El orden debe ser mayor que cero")
    private Integer orden;
}
