package com.pesapp.pesapp.peso.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pesapp.pesapp.config.FlexibleBigDecimalDeserializer;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PesoHoyRequestDto {

    @Size(max = 120, message = "El clientId no puede superar 120 caracteres")
    private String clientId;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "1.0", inclusive = true, message = "El peso debe ser mayor que 0")
    @DecimalMax(value = "999.99", inclusive = true, message = "El peso no puede superar 999.99")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal peso;

    private Long version;
}
