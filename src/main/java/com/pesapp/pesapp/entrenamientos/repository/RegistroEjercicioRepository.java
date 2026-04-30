package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroEjercicioRepository extends JpaRepository<RegistroEjercicioVO, Long> {

    Optional<RegistroEjercicioVO>
            findFirstByEjercicioCatalogo_IdAndRegistroEntrenamiento_Usuario_IdAndRegistroEntrenamiento_DeletedAtIsNullAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
            Long ejercicioId,
            Long usuarioId);

    Optional<RegistroEjercicioVO>
            findFirstByPlantillaEjercicio_IdAndRegistroEntrenamiento_Usuario_IdAndRegistroEntrenamiento_DeletedAtIsNullAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
            Long plantillaEjercicioId,
            Long usuarioId);

    @Query("""
            select distinct ejercicio
              from RegistroEjercicioVO ejercicio
              join fetch ejercicio.registroEntrenamiento entrenamiento
              left join fetch ejercicio.seriesRealizadas series
              left join fetch ejercicio.ejercicioCatalogo catalogo
             left join fetch ejercicio.plantillaEjercicio plantilla
             left join fetch plantilla.ejercicioCatalogo plantillaCatalogo
             where entrenamiento.usuario.id = :usuarioId
               and entrenamiento.deletedAt is null
               and ejercicio.omitido = false
             order by entrenamiento.fechaFinalizacion desc, entrenamiento.id desc, ejercicio.id desc, series.orden asc
            """)
    List<RegistroEjercicioVO> findHistoricoVisibleByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("""
            update RegistroEjercicioVO ejercicio
               set ejercicio.ejercicioCatalogo = null
             where ejercicio.ejercicioCatalogo.id = :ejercicioId
               and ejercicio.registroEntrenamiento.usuario.id = :usuarioId
            """)
    int desvincularCatalogo(@Param("ejercicioId") Long ejercicioId, @Param("usuarioId") Long usuarioId);
}
