package com.pesapp.pesapp.usuarios.repository;

import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioVO, Long> {

    Optional<UsuarioVO> findByEmailIgnoreCase(String email);

    Optional<UsuarioVO> findByUsernameIgnoreCase(String username);

    Optional<UsuarioVO> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    Optional<UsuarioVO> findFirstByOrderByIdAsc();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
}
