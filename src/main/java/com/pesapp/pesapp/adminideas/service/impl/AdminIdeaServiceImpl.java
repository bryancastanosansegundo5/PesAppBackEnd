package com.pesapp.pesapp.adminideas.service.impl;

import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaCreateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaEstadoUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaResponseDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.vo.AdminIdeaVO;
import com.pesapp.pesapp.adminideas.repository.AdminIdeaRepository;
import com.pesapp.pesapp.adminideas.service.AdminIdeaService;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminIdeaServiceImpl implements AdminIdeaService {

    private final AdminIdeaRepository adminIdeaRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminIdeaResponseDto> obtenerIdeas() {
        usuarioService.obtenerUsuarioAutenticado();
        return adminIdeaRepository.findAllByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminIdeaResponseDto guardarIdea(AdminIdeaCreateRequestDto request) {
        usuarioService.obtenerUsuarioAutenticado();

        String clientId = normalizarObligatorio(request.getClientId(), "El clientId es obligatorio");
        AdminIdeaVO idea = adminIdeaRepository.findByClientId(clientId).orElseGet(AdminIdeaVO::new);

        if (idea.getId() == null) {
            idea.setClientId(clientId);
        }

        idea.setTitulo(normalizarObligatorio(request.getTitulo(), "El titulo es obligatorio"));
        idea.setDescripcion(normalizarObligatorio(request.getDescripcion(), "La descripcion es obligatoria"));
        idea.setCompletada(Boolean.TRUE.equals(request.getCompletada()));
        idea.setActivo(validarBooleanoObligatorio(request.getActivo(), "El activo es obligatorio"));

        try {
            return toResponse(adminIdeaRepository.saveAndFlush(idea));
        } catch (DataIntegrityViolationException exception) {
            AdminIdeaVO existente = adminIdeaRepository.findByClientId(clientId)
                    .orElseThrow(() -> exception);
            return toResponse(existente);
        }
    }

    @Override
    @Transactional
    public AdminIdeaResponseDto actualizarIdea(Long id, AdminIdeaUpdateRequestDto request) {
        usuarioService.obtenerUsuarioAutenticado();

        AdminIdeaVO idea = adminIdeaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la idea administrativa con id " + id));

        validarVersion(request.getVersion(), idea);
        if (request.getTitulo() != null) {
            idea.setTitulo(normalizarObligatorio(request.getTitulo(), "El titulo es obligatorio"));
        }
        if (request.getDescripcion() != null) {
            idea.setDescripcion(normalizarObligatorio(request.getDescripcion(), "La descripcion es obligatoria"));
        }
        if (request.getCompletada() != null) {
            idea.setCompletada(request.getCompletada());
        }
        if (request.getActivo() != null) {
            idea.setActivo(request.getActivo());
        }

        return toResponse(adminIdeaRepository.saveAndFlush(idea));
    }

    @Override
    @Transactional
    public AdminIdeaResponseDto actualizarEstado(Long id, AdminIdeaEstadoUpdateRequestDto request) {
        usuarioService.obtenerUsuarioAutenticado();

        AdminIdeaVO idea = adminIdeaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la idea administrativa con id " + id));

        validarVersion(request.getVersion(), idea);
        idea.setActivo(validarBooleanoObligatorio(request.getActivo(), "El activo es obligatorio"));

        return toResponse(adminIdeaRepository.saveAndFlush(idea));
    }

    private void validarVersion(Long versionEsperada, AdminIdeaVO idea) {
        if (!versionEsperada.equals(idea.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private AdminIdeaResponseDto toResponse(AdminIdeaVO idea) {
        AdminIdeaResponseDto response = new AdminIdeaResponseDto();
        response.setId(String.valueOf(idea.getId()));
        response.setClientId(idea.getClientId());
        response.setTitulo(idea.getTitulo());
        response.setDescripcion(idea.getDescripcion());
        response.setCompletada(idea.isCompletada());
        response.setActivo(idea.isActivo());
        response.setCreatedAt(idea.getCreatedAt());
        response.setUpdatedAt(idea.getUpdatedAt());
        response.setVersion(idea.getVersion());
        return response;
    }

    private boolean validarBooleanoObligatorio(Boolean value, String mensajeError) {
        if (value == null) {
            throw new IllegalArgumentException(mensajeError);
        }

        return value;
    }

    private String normalizarObligatorio(String value, String mensajeError) {
        if (value == null) {
            throw new IllegalArgumentException(mensajeError);
        }

        String normalizado = value.trim();
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }

        return normalizado;
    }
}
