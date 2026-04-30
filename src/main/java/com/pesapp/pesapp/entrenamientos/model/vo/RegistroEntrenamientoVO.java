package com.pesapp.pesapp.entrenamientos.model.vo;

import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "registros_entrenamiento",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_registros_entrenamiento_usuario_id_frontend",
                    columnNames = {"usuario_id", "id_frontend"})
        })
public class RegistroEntrenamientoVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_sesion_id")
    private PlantillaSesionEntrenamientoVO plantillaSesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioVO usuario;

    @Column(nullable = false, length = 150)
    private String nombreSesion;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFinalizacion;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "registroEntrenamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroEjercicioVO> ejercicios = new ArrayList<>();

    public void addEjercicio(RegistroEjercicioVO ejercicio) {
        ejercicios.add(ejercicio);
        ejercicio.setRegistroEntrenamiento(this);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
