package com.pesapp.pesapp.entrenamientos.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaEjercicioRequestDto {

    private Long id;

    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(max = 150, message = "El nombre del ejercicio no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
    private String descripcion;

    @NotNull(message = "Las series base son obligatorias")
    @Min(value = 0, message = "Las series base no pueden ser negativas")
    private Integer seriesBase;

    @NotNull(message = "Las repeticiones base son obligatorias")
    @Min(value = 0, message = "Las repeticiones base no pueden ser negativas")
    private Integer repeticionesBase;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso base no puede ser negativo")
    private BigDecimal pesoBase;

    @DecimalMin(value = "0.0", inclusive = true, message = "La altura del banco no puede ser negativa")
    private BigDecimal alturaBanco;

    @Size(max = 100, message = "El agarre no puede superar 100 caracteres")
    private String agarre;
}
