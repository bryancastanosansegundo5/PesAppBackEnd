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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "plantillas_ejercicio",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_plantillas_ejercicio_sesion_id_frontend",
                    columnNames = {"plantilla_sesion_id", "id_frontend"})
        })
public class PlantillaEjercicioVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_sesion_id", nullable = false)
    private PlantillaSesionEntrenamientoVO plantillaSesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id")
    private EjercicioVO ejercicioCatalogo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 2000)
    private String observaciones;

    @Column(length = 100)
    private String grupoMuscular;

    @Column(length = 100)
    private String patronMovimiento;

    @Column(length = 120)
    private String equipamiento;

    @Column(nullable = false)
    private Integer seriesBase;

    @Column(nullable = false)
    private Integer repeticionesBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal pesoBase;

    @Column(length = 100)
    private String alturaBanco;

    @Column(length = 100)
    private String agarre;
}
