package com.pesapp.pesapp.peso.service.impl;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import com.pesapp.pesapp.peso.model.vo.PesoCorporalVO;
import com.pesapp.pesapp.peso.repository.PesoCorporalRepository;
import com.pesapp.pesapp.peso.service.PesoCorporalService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PesoCorporalServiceImpl implements PesoCorporalService {

    private final PesoCorporalRepository pesoCorporalRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<PesoCorporalResponseDto> obtenerHistorico() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return pesoCorporalRepository.findAllByUsuario_IdOrderByFechaRegistroDescCreatedAtDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PesoCorporalResponseDto guardarPesoHoy(PesoHoyRequestDto request) {
        PesoCorporalRequestDto payload = new PesoCorporalRequestDto();
        payload.setClientId(request.getClientId());
        payload.setPeso(request.getPeso());
        payload.setFechaRegistro(LocalDate.now());
        payload.setVersion(request.getVersion());
        return guardar(payload);
    }

    @Override
    @Transactional
    public PesoCorporalResponseDto guardar(PesoCorporalRequestDto request) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        LocalDate fechaRegistro = request.getFechaRegistro() == null ? LocalDate.now() : request.getFechaRegistro();
        String clientId = normalizarNullable(request.getClientId());

        PesoCorporalVO peso = buscarParaUpsert(usuario.getId(), fechaRegistro, clientId);
        if (peso.getId() == null) {
            peso.setUsuario(usuario);
            peso.setFechaRegistro(fechaRegistro);
            peso.setClientId(clientId);
        }

        validarVersion(request.getVersion(), peso);
        peso.setPeso(request.getPeso().stripTrailingZeros());
        if (peso.getClientId() == null) {
            peso.setClientId(clientId);
        }

        return toResponse(pesoCorporalRepository.saveAndFlush(peso));
    }

    private PesoCorporalVO buscarParaUpsert(Long usuarioId, LocalDate fechaRegistro, String clientId) {
        if (clientId != null) {
            PesoCorporalVO peso = pesoCorporalRepository
                    .findFirstByClientIdAndUsuario_IdOrderByIdDesc(clientId, usuarioId)
                    .orElse(null);
            if (peso != null) {
                return peso;
            }
        }

        return pesoCorporalRepository.findByUsuario_IdAndFechaRegistro(usuarioId, fechaRegistro).orElse(new PesoCorporalVO());
    }

    private void validarVersion(Long versionEsperada, PesoCorporalVO peso) {
        if (versionEsperada != null && peso.getId() != null && !versionEsperada.equals(peso.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private PesoCorporalResponseDto toResponse(PesoCorporalVO peso) {
        PesoCorporalResponseDto response = new PesoCorporalResponseDto();
        response.setId(String.valueOf(peso.getId()));
        response.setUserId(peso.getUsuario().getId());
        response.setClientId(peso.getClientId());
        response.setPeso(peso.getPeso());
        response.setFechaRegistro(peso.getFechaRegistro());
        response.setCreatedAt(peso.getCreatedAt());
        response.setUpdatedAt(peso.getUpdatedAt());
        response.setVersion(peso.getVersion());
        return response;
    }

    private String normalizarNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
