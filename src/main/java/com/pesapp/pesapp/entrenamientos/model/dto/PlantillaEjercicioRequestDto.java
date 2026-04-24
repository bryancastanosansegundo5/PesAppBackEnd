package com.pesapp.pesapp.entrenamientos.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String catalogoEjercicioId;

    @NotBlank(message = "El nombre del ejercicio es obligatorio")
    @Size(max = 150, message = "El nombre del ejercicio no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El grupo muscular no puede superar 100 caracteres")
    private String grupoMuscular;

    @Size(max = 100, message = "El patron de movimiento no puede superar 100 caracteres")
    private String patronMovimiento;

    @Size(max = 120, message = "El equipamiento no puede superar 120 caracteres")
    private String equipamiento;

    @NotNull(message = "Las series base son obligatorias")
    @Min(value = 0, message = "Las series base no pueden ser negativas")
    private Integer seriesPlanificadas;

    @NotNull(message = "Las repeticiones base son obligatorias")
    @Min(value = 0, message = "Las repeticiones base no pueden ser negativas")
    private Integer repeticionesPlanificadas;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso base no puede ser negativo")
    private BigDecimal pesoPlanificado;

    @Size(max = 100, message = "La altura del banco no puede superar 100 caracteres")
    private String alturaBanco;

    @Size(max = 100, message = "El agarre no puede superar 100 caracteres")
    private String agarre;

    private boolean completado;
    private boolean omitido;

    @Valid
    private List<RegistroSerieRequestDto> seriesRealizadas = new ArrayList<>();
}
