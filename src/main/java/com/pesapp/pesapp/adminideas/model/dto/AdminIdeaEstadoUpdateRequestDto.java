package com.pesapp.pesapp.adminideas.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminIdeaEstadoUpdateRequestDto {

    @NotNull(message = "El activo es obligatorio")
    private Boolean activo;

    @NotNull(message = "La version es obligatoria")
    private Long version;
}
