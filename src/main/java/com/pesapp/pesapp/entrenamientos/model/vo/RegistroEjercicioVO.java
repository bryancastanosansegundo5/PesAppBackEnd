package com.pesapp.pesapp.entrenamientos.model.vo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registros_ejercicio")
public class RegistroEjercicioVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_entrenamiento_id", nullable = false)
    private RegistroEntrenamientoVO registroEntrenamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_ejercicio_id")
    private PlantillaEjercicioVO plantillaEjercicio;

    @Column(nullable = false, length = 150)
    private String nombre;

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

    @Column(nullable = false)
    private boolean completado;

    @Column(nullable = false)
    private boolean omitido;

    @OneToMany(mappedBy = "registroEjercicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroSerieVO> seriesRealizadas = new ArrayList<>();

    public void addSerieRealizada(RegistroSerieVO serie) {
        seriesRealizadas.add(serie);
        serie.setRegistroEjercicio(this);
    }
}
