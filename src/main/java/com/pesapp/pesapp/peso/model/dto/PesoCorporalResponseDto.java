package com.pesapp.pesapp.peso.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PesoCorporalResponseDto {

    private String id;
    private Long userId;
    private String clientId;
    private BigDecimal peso;
    private LocalDate fechaRegistro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
