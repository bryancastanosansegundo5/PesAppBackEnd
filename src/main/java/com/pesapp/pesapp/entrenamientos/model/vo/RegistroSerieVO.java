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
        name = "registros_serie",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_registros_serie_ejercicio_id_frontend",
                    columnNames = {"registro_ejercicio_id", "id_frontend"})
        })
public class RegistroSerieVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_ejercicio_id", nullable = false)
    private RegistroEjercicioVO registroEjercicio;

    @Column(nullable = false)
    private Integer numeroSerie;

    @Column(nullable = false)
    private Integer repeticiones;

    @Column(precision = 10, scale = 2)
    private BigDecimal peso;

    @Column(nullable = false)
    private Integer orden;
}
