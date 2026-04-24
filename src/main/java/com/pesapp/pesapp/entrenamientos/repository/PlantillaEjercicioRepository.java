package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaEjercicioRepository extends JpaRepository<PlantillaEjercicioVO, Long> {

    Optional<PlantillaEjercicioVO> findByIdAndPlantillaSesion_Usuario_Id(Long id, Long usuarioId);

    Optional<PlantillaEjercicioVO> findFirstByIdFrontendAndPlantillaSesion_Usuario_IdOrderByIdDesc(
            String idFrontend,
            Long usuarioId);

    Optional<PlantillaEjercicioVO> findFirstByIdFrontendAndPlantillaSesion_IdOrderByIdDesc(
            String idFrontend,
            Long plantillaSesionId);

    boolean existsByIdAndPlantillaSesion_Usuario_Id(Long id, Long usuarioId);

    boolean existsByIdFrontendAndPlantillaSesion_Usuario_Id(String idFrontend, Long usuarioId);
}
