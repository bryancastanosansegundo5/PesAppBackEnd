package com.pesapp.pesapp.entrenamientos.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plantillas_ejercicio")
public class PlantillaEjercicioVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_sesion_id", nullable = false)
    private PlantillaSesionEntrenamientoVO plantillaSesion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Integer seriesBase;

    @Column(nullable = false)
    private Integer repeticionesBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal pesoBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal alturaBanco;

    @Column(length = 100)
    private String agarre;
}
