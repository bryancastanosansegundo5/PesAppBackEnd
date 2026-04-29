package com.pesapp.pesapp.adminideas.model.vo;

import com.pesapp.pesapp.entrenamientos.model.vo.AuditoriaVO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "admin_ideas",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_admin_ideas_client_id", columnNames = "client_id")
        })
public class AdminIdeaVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 120)
    private String clientId;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(nullable = false)
    private boolean completada;

    @Column(nullable = false)
    private boolean activo = true;
}
