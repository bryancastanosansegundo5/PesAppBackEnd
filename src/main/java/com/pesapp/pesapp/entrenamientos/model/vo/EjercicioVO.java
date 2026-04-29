package com.pesapp.pesapp.entrenamientos.model.vo;

import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
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
        name = "ejercicios",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_ejercicios_usuario_client_id",
                    columnNames = {"usuario_id", "client_id"})
        })
public class EjercicioVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", length = 120)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioVO usuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 100)
    private String grupoMuscular;

    @Column(length = 100)
    private String patronMovimiento;

    @Column(length = 120)
    private String equipamiento;

    @Column(nullable = false)
    private Integer seriesPlanificadas;

    @Column(nullable = false)
    private Integer repeticionesPlanificadas;

    @Column(precision = 10, scale = 2)
    private BigDecimal pesoPlanificado;

    @Column(length = 100)
    private String alturaBanco;

    @Column(length = 100)
    private String agarre;
}
