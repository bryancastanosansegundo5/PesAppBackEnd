package com.pesapp.pesapp.usuarios.model.vo;

import com.pesapp.pesapp.entrenamientos.model.vo.AuditoriaVO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_usuarios_email", columnNames = "email"),
            @UniqueConstraint(name = "uq_usuarios_username", columnNames = "username")
        })
public class UsuarioVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(length = 180)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol = RolUsuario.USUARIO;

    @Column(nullable = false)
    private boolean activo = true;
}
