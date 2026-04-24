package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroEjercicioRepository extends JpaRepository<RegistroEjercicioVO, Long> {

    Optional<RegistroEjercicioVO> findFirstByPlantillaEjercicio_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
            Long plantillaEjercicioId,
            Long usuarioId);
}
