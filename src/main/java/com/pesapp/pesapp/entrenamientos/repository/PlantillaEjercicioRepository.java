package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("""
            update PlantillaEjercicioVO ejercicio
               set ejercicio.ejercicioCatalogo = null
             where ejercicio.ejercicioCatalogo.id = :ejercicioId
               and ejercicio.plantillaSesion.usuario.id = :usuarioId
            """)
    int desvincularCatalogo(@Param("ejercicioId") Long ejercicioId, @Param("usuarioId") Long usuarioId);
}
