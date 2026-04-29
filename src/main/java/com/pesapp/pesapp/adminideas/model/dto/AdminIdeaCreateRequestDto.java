package com.pesapp.pesapp.adminideas.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminIdeaCreateRequestDto {

    @NotBlank(message = "El clientId es obligatorio")
    @Size(max = 120, message = "El clientId no puede superar 120 caracteres")
    private String clientId;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 160, message = "El titulo no puede superar 160 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 2000, message = "La descripcion no puede superar 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La completada es obligatoria")
    private Boolean completada;

    @NotNull(message = "El activo es obligatorio")
    private Boolean activo;
}
