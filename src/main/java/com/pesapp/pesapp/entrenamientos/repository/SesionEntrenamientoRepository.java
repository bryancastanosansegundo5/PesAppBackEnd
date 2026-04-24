package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionEntrenamientoRepository extends JpaRepository<PlantillaSesionEntrenamientoVO, Long> {

    List<PlantillaSesionEntrenamientoVO> findAllByUsuario_IdOrderByNombreAsc(Long usuarioId);

    Optional<PlantillaSesionEntrenamientoVO> findByIdAndUsuario_Id(Long id, Long usuarioId);

    Optional<PlantillaSesionEntrenamientoVO> findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(String idFrontend, Long usuarioId);
}
