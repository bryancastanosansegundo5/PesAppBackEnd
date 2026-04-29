package com.pesapp.pesapp.adminideas.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminIdeaUpdateRequestDto {

    @Size(max = 160, message = "El titulo no puede superar 160 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripcion no puede superar 2000 caracteres")
    private String descripcion;

    private Boolean completada;

    private Boolean activo;

    @NotNull(message = "La version es obligatoria")
    private Long version;
}
