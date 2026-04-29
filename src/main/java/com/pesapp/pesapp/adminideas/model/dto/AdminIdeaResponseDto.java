package com.pesapp.pesapp.adminideas.model.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminIdeaResponseDto {

    private String id;
    private String clientId;
    private String titulo;
    private String descripcion;
    private boolean completada;
    private boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
