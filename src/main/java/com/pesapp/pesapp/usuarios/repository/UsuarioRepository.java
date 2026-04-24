package com.pesapp.pesapp.usuarios.repository;

import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioVO, Long> {

    Optional<UsuarioVO> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
