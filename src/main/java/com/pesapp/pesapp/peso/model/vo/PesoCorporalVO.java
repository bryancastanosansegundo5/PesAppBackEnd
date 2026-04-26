package com.pesapp.pesapp.peso.model.vo;

import com.pesapp.pesapp.entrenamientos.model.vo.AuditoriaVO;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "pesos_corporales",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_pesos_corporales_usuario_client_id", columnNames = {"usuario_id", "client_id"})
        })
public class PesoCorporalVO extends AuditoriaVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioVO usuario;

    @Column(name = "client_id", length = 120)
    private String clientId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal peso;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "hora_registro", nullable = false, length = 5)
    private String horaRegistro;

    @Column(name = "hora_manual", nullable = false)
    private boolean horaManual;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
