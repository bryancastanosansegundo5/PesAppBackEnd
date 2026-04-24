package com.pesapp.pesapp.entrenamientos.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtroEntrenoResumenDto {

    private String id;
    private String nombre;
    private String descripcion;
    private String grupoMuscular;
    private String patronMovimiento;
    private String equipamiento;
    private String agarre;
    private String alturaBanco;
    private Integer sesionesTotales;
    private LocalDateTime ultimoRegistro;
    private BigDecimal pesoMaximoHistorico;
    private BigDecimal volumenHistorico;
    private List<OtroEntrenoChartPointDto> chartData = new ArrayList<>();
    private List<OtroEntrenoEntradaDto> entradas = new ArrayList<>();
}
