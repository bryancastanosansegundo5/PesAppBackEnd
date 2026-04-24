package com.pesapp.pesapp.entrenamientos.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pesapp.pesapp.config.FlexibleBigDecimalDeserializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistroSerieRequestDto {

    private String id;

    @NotNull(message = "El numero de serie es obligatorio")
    @Min(value = 1, message = "El numero de serie debe ser mayor que cero")
    private Integer numeroSerie;

    @NotNull(message = "Las repeticiones son obligatorias")
    @Min(value = 0, message = "Las repeticiones no pueden ser negativas")
    private Integer repeticiones;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso no puede ser negativo")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal peso;
}
