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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plantillas_sesion_entrenamiento")
public class PlantillaSesionEntrenamientoVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String idFrontend;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioVO usuario;

    @OneToMany(mappedBy = "plantillaSesion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlantillaEjercicioVO> ejercicios = new ArrayList<>();

    public void addEjercicio(PlantillaEjercicioVO ejercicio) {
        ejercicios.add(ejercicio);
        ejercicio.setPlantillaSesion(this);
    }

    public void limpiarEjercicios() {
        ejercicios.forEach(ejercicio -> ejercicio.setPlantillaSesion(null));
        ejercicios.clear();
    }
}
