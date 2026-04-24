package com.pesapp.pesapp.entrenamientos.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pesapp.pesapp.config.FlexibleBigDecimalDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantillaEjercicioRequestDto {

    private String idEjercicio;

    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(max = 150, message = "El nombre del ejercicio no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "Las series base son obligatorias")
    @Min(value = 0, message = "Las series base no pueden ser negativas")
    private Integer seriesPlanificadas;

    @NotNull(message = "Las repeticiones base son obligatorias")
    @Min(value = 0, message = "Las repeticiones base no pueden ser negativas")
    private Integer repeticionesPlanificadas;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso base no puede ser negativo")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal pesoPlanificado;

    @DecimalMin(value = "0.0", inclusive = true, message = "La altura del banco no puede ser negativa")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal alturaBanco;

    @Size(max = 100, message = "El agarre no puede superar 100 caracteres")
    private String agarre;

    private boolean completado;
    private boolean omitido;

    @Valid
    private List<RegistroSerieRequestDto> seriesRealizadas = new ArrayList<>();
}
