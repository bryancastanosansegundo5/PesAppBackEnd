package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEntrenamientoVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrenamientoRepository extends JpaRepository<RegistroEntrenamientoVO, Long> {

    List<RegistroEntrenamientoVO> findAllByUsuario_IdOrderByFechaFinalizacionDescFechaInicioDesc(Long usuarioId);

    Optional<RegistroEntrenamientoVO> findByIdAndUsuario_Id(Long id, Long usuarioId);

    Optional<RegistroEntrenamientoVO> findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(String idFrontend, Long usuarioId);

    Optional<RegistroEntrenamientoVO> findFirstByPlantillaSesion_IdAndUsuario_IdOrderByFechaFinalizacionDescFechaInicioDescIdDesc(
            Long plantillaSesionId,
            Long usuarioId);
}
