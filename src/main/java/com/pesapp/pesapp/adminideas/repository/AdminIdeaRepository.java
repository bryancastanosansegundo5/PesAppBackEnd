package com.pesapp.pesapp.adminideas.repository;

import com.pesapp.pesapp.adminideas.model.vo.AdminIdeaVO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminIdeaRepository extends JpaRepository<AdminIdeaVO, Long> {

    List<AdminIdeaVO> findAllByOrderByUpdatedAtDescIdDesc();

    Optional<AdminIdeaVO> findByClientId(String clientId);
}
