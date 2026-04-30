package com.pesapp.pesapp.peso.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pesapp.pesapp.config.FlexibleBigDecimalDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PesoCorporalRequestDto {

    @Size(max = 120, message = "El clientId no puede superar 120 caracteres")
    private String clientId;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "1.0", inclusive = true, message = "El peso debe ser mayor que 0")
    @DecimalMax(value = "999.99", inclusive = true, message = "El peso no puede superar 999.99")
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal peso;

    private LocalDate fechaRegistro;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La horaRegistro debe tener formato HH:mm")
    private String horaRegistro;

    @NotNull(message = "El indicador horaManual es obligatorio")
    private Boolean horaManual;

    @Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
    private String comentario;

    private Long version;
}
