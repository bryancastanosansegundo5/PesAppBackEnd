package com.pesapp.pesapp.usuarios.repository;

import com.pesapp.pesapp.usuarios.model.vo.RefreshTokenVO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenVO, Long> {

    Optional<RefreshTokenVO> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshTokenVO token
               set token.revokedAt = :revokedAt
             where token.tokenHash = :tokenHash
               and token.revokedAt is null
            """)
    int revocarPorHash(@Param("tokenHash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt);
}
