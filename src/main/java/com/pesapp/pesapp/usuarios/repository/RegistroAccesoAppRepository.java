package com.pesapp.pesapp.usuarios.repository;

import com.pesapp.pesapp.usuarios.model.vo.RegistroAccesoAppVO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAccesoAppRepository extends JpaRepository<RegistroAccesoAppVO, Long> {

    List<RegistroAccesoAppVO> findAllByFechaAccesoBetween(LocalDateTime desde, LocalDateTime hasta);
}
