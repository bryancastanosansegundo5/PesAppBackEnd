package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroEjercicioRepository extends JpaRepository<RegistroEjercicioVO, Long> {

    Optional<RegistroEjercicioVO> findFirstByEjercicioCatalogo_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
            Long ejercicioId,
            Long usuarioId);

    Optional<RegistroEjercicioVO> findFirstByPlantillaEjercicio_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
            Long plantillaEjercicioId,
            Long usuarioId);

    @Modifying
    @Query("""
            update RegistroEjercicioVO ejercicio
               set ejercicio.ejercicioCatalogo = null
             where ejercicio.ejercicioCatalogo.id = :ejercicioId
               and ejercicio.registroEntrenamiento.usuario.id = :usuarioId
            """)
    int desvincularCatalogo(@Param("ejercicioId") Long ejercicioId, @Param("usuarioId") Long usuarioId);
}
