package com.pesapp.pesapp.peso.service.impl;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import com.pesapp.pesapp.peso.model.vo.PesoCorporalVO;
import com.pesapp.pesapp.peso.repository.PesoCorporalRepository;
import com.pesapp.pesapp.peso.service.PesoCorporalService;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PesoCorporalServiceImpl implements PesoCorporalService {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PesoCorporalRepository pesoCorporalRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<PesoCorporalResponseDto> obtenerHistorico() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return pesoCorporalRepository.findAllByUsuario_IdOrderByFechaDescCreatedAtDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PesoCorporalResponseDto guardarPesoHoy(PesoHoyRequestDto request) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        LocalDate hoy = LocalDate.now();
        String clientId = normalizarNullable(request.getClientId());

        PesoCorporalVO peso = buscarParaUpsert(usuario.getId(), clientId);
        return guardarInterno(
                usuario,
                peso,
                peso.getId() == null ? hoy : peso.getFechaRegistro(),
                request.getPeso(),
                request.getHoraRegistro(),
                request.getHoraManual(),
                request.getComentario(),
                clientId,
                request.getVersion());
    }

    @Override
    @Transactional
    public PesoCorporalResponseDto guardar(PesoCorporalRequestDto request) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        LocalDate fechaRegistro = request.getFechaRegistro() == null ? LocalDate.now() : request.getFechaRegistro();
        String clientId = normalizarNullable(request.getClientId());

        PesoCorporalVO peso = buscarParaUpsert(usuario.getId(), clientId);
        return guardarInterno(
                usuario,
                peso,
                fechaRegistro,
                request.getPeso(),
                request.getHoraRegistro(),
                request.getHoraManual(),
                request.getComentario(),
                clientId,
                request.getVersion());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        PesoCorporalVO peso = pesoCorporalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el peso corporal con id " + id));

        if (!puedeEliminar(usuario, peso)) {
            throw new AccessDeniedException("No tienes permisos para eliminar este registro de peso");
        }

        pesoCorporalRepository.delete(peso);
    }

    private PesoCorporalVO buscarParaUpsert(Long usuarioId, String clientId) {
        if (clientId != null) {
            PesoCorporalVO peso = pesoCorporalRepository
                    .findFirstByClientIdAndUsuario_IdOrderByIdDesc(clientId, usuarioId)
                    .orElse(null);
            if (peso != null) {
                return peso;
            }
        }

        return new PesoCorporalVO();
    }

    private void validarVersion(Long versionEsperada, PesoCorporalVO peso) {
        if (versionEsperada != null && peso.getId() != null && !versionEsperada.equals(peso.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private PesoCorporalResponseDto guardarInterno(
            UsuarioVO usuario,
            PesoCorporalVO peso,
            LocalDate fechaRegistroObjetivo,
            java.math.BigDecimal pesoRequest,
            String horaRegistroRequest,
            Boolean horaManualRequest,
            String comentarioRequest,
            String clientId,
            Long versionEsperada) {
        if (peso.getId() == null) {
            peso.setUsuario(usuario);
            peso.setFechaRegistro(fechaRegistroObjetivo);
            peso.setClientId(clientId);
        }

        LocalDate fechaRegistroFinal = peso.getId() == null ? fechaRegistroObjetivo : peso.getFechaRegistro();

        validarVersion(versionEsperada, peso);
        peso.setPeso(pesoRequest.stripTrailingZeros());
        peso.setHoraManual(Boolean.TRUE.equals(horaManualRequest));
        String horaRegistroFinal = resolverHoraRegistro(horaRegistroRequest, peso.isHoraManual(), fechaRegistroFinal);
        peso.setHoraRegistro(horaRegistroFinal);
        peso.setComentario(normalizarNullable(comentarioRequest));
        peso.setFecha(LocalDateTime.of(fechaRegistroFinal, parseHora(horaRegistroFinal)));
        if (peso.getClientId() == null) {
            peso.setClientId(clientId);
        }

        return toResponse(pesoCorporalRepository.saveAndFlush(peso));
    }

    private PesoCorporalResponseDto toResponse(PesoCorporalVO peso) {
        PesoCorporalResponseDto response = new PesoCorporalResponseDto();
        response.setId(String.valueOf(peso.getId()));
        response.setUserId(peso.getUsuario().getId());
        response.setClientId(peso.getClientId());
        response.setPeso(peso.getPeso());
        response.setFechaRegistro(peso.getFechaRegistro());
        response.setHoraRegistro(peso.getHoraRegistro());
        response.setHoraManual(peso.isHoraManual());
        response.setComentario(peso.getComentario());
        response.setFecha(peso.getFecha());
        response.setCreatedAt(peso.getCreatedAt());
        response.setUpdatedAt(peso.getUpdatedAt());
        response.setVersion(peso.getVersion());
        return response;
    }

    private boolean puedeEliminar(UsuarioVO usuario, PesoCorporalVO peso) {
        return usuario.getRol() == RolUsuario.ADMIN || peso.getUsuario().getId().equals(usuario.getId());
    }

    private String resolverHoraRegistro(String horaRegistroRequest, boolean horaManual, LocalDate fechaRegistro) {
        if (horaManual) {
            if (horaRegistroRequest == null || horaRegistroRequest.isBlank()) {
                throw new IllegalArgumentException("La horaRegistro es obligatoria cuando horaManual es true");
            }

            return formatHora(horaRegistroRequest);
        }

        String horaBase = horaRegistroRequest;
        if (horaBase == null || horaBase.isBlank()) {
            LocalDateTime now = LocalDateTime.now();
            horaBase = fechaRegistro.equals(now.toLocalDate())
                    ? now.toLocalTime().format(HORA_FORMATTER)
                    : LocalTime.MIDNIGHT.format(HORA_FORMATTER);
        }

        return formatHora(horaBase);
    }

    private String formatHora(String horaRegistro) {
        return parseHora(horaRegistro).format(HORA_FORMATTER);
    }

    private LocalTime parseHora(String horaRegistro) {
        try {
            return LocalTime.parse(horaRegistro, HORA_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La horaRegistro debe tener formato HH:mm");
        }
    }

    private String normalizarNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
