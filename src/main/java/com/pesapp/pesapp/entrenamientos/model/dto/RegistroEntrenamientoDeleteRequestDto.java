package com.pesapp.pesapp.entrenamientos.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistroEntrenamientoDeleteRequestDto {

    private String id;
    private String clientId;
    private Long version;
}
