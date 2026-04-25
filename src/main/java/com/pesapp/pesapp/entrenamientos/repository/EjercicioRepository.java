package com.pesapp.pesapp.entrenamientos.repository;

import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjercicioRepository extends JpaRepository<EjercicioVO, Long> {

    List<EjercicioVO> findAllByUsuario_IdOrderByNombreAsc(Long usuarioId);

    Optional<EjercicioVO> findByIdAndUsuario_Id(Long id, Long usuarioId);

    Optional<EjercicioVO> findFirstByClientIdAndUsuario_IdOrderByIdDesc(String clientId, Long usuarioId);

    boolean existsByIdAndUsuario_Id(Long id, Long usuarioId);
}
