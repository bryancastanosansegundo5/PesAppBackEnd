package com.pesapp.pesapp.peso.repository;

import com.pesapp.pesapp.peso.model.vo.PesoCorporalVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesoCorporalRepository extends JpaRepository<PesoCorporalVO, Long> {

    List<PesoCorporalVO> findAllByUsuario_IdOrderByFechaDescCreatedAtDesc(Long usuarioId);

    Optional<PesoCorporalVO> findByIdAndUsuario_Id(Long id, Long usuarioId);

    Optional<PesoCorporalVO> findFirstByClientIdAndUsuario_IdOrderByIdDesc(String clientId, Long usuarioId);
}
