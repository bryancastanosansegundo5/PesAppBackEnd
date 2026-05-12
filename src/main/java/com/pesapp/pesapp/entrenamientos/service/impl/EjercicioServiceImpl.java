package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.service.EjercicioService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
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
public class EjercicioServiceImpl implements EjercicioService {

    private final EjercicioRepository ejercicioRepository;
    private final PlantillaEjercicioRepository plantillaEjercicioRepository;
    private final RegistroEjercicioRepository registroEjercicioRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<EjercicioResponseDto> obtenerTodos() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return ejercicioRepository.findAllByUsuario_IdOrderByNombreAsc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EjercicioResponseDto obtenerPorId(Long id) {
        return toResponse(buscarEjercicio(id));
    }

    @Override
    @Transactional
    public EjercicioResponseDto crear(EjercicioRequestDto request) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        EjercicioVO ejercicio = buscarParaUpsert(request, usuario);
        if (ejercicio.getId() == null) {
            ejercicio.setUsuario(usuario);
            ejercicio.setClientId(normalizarNullable(request.getClientId()));
        }

        validarVersion(request.getVersion(), ejercicio);
        copiarCampos(request, ejercicio);
        return toResponse(guardarEjercicioIdempotente(ejercicio, request, usuario.getId()));
    }

    @Override
    @Transactional
    public EjercicioResponseDto actualizar(Long id, EjercicioRequestDto request) {
        EjercicioVO ejercicio = buscarEjercicio(id);
        validarVersion(request.getVersion(), ejercicio);
        if (ejercicio.getClientId() == null) {
            ejercicio.setClientId(normalizarNullable(request.getClientId()));
        }
        copiarCampos(request, ejercicio);
        return toResponse(guardarEjercicioIdempotente(ejercicio, request, ejercicio.getUsuario().getId()));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        EjercicioVO ejercicio = buscarEjercicio(id);
        plantillaEjercicioRepository.desvincularCatalogo(id, usuario.getId());
        registroEjercicioRepository.desvincularCatalogo(id, usuario.getId());
        ejercicioRepository.delete(ejercicio);
    }

    private EjercicioVO buscarEjercicio(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return ejercicioRepository.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("No existe el ejercicio con id " + id));
    }

    private EjercicioVO guardarEjercicioIdempotente(EjercicioVO ejercicio, EjercicioRequestDto request, Long usuarioId) {
        try {
            return ejercicioRepository.saveAndFlush(ejercicio);
        } catch (DataIntegrityViolationException exception) {
            String clientId = normalizarNullable(request.getClientId());
            if (clientId != null) {
                EjercicioVO existente = ejercicioRepository
                        .findFirstByClientIdAndUsuario_IdOrderByIdDesc(clientId, usuarioId)
                        .orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            if (ejercicio.getId() != null) {
                EjercicioVO existente = ejercicioRepository.findByIdAndUsuario_Id(ejercicio.getId(), usuarioId).orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            throw exception;
        }
    }

    private void copiarCampos(EjercicioRequestDto request, EjercicioVO ejercicio) {
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setObservaciones(normalizarNullable(request.getObservaciones()));
        ejercicio.setGrupoMuscular(normalizarNullable(request.getGrupoMuscular()));
        ejercicio.setPatronMovimiento(normalizarNullable(request.getPatronMovimiento()));
        ejercicio.setEquipamiento(normalizarNullable(request.getEquipamiento()));
        ejercicio.setSeriesPlanificadas(request.getSeriesPlanificadas());
        ejercicio.setRepeticionesPlanificadas(request.getRepeticionesPlanificadas());
        ejercicio.setPesoPlanificado(request.getPesoPlanificado());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
    }

    private EjercicioVO buscarParaUpsert(EjercicioRequestDto request, UsuarioVO usuario) {
        Long id = parseId(request.getId());
        if (id != null) {
            EjercicioVO ejercicio = ejercicioRepository.findByIdAndUsuario_Id(id, usuario.getId()).orElse(null);
            if (ejercicio != null) {
                return ejercicio;
            }
        }

        String clientId = normalizarNullable(request.getClientId());
        if (clientId != null) {
            EjercicioVO ejercicio = ejercicioRepository
                    .findFirstByClientIdAndUsuario_IdOrderByIdDesc(clientId, usuario.getId())
                    .orElse(null);
            if (ejercicio != null) {
                return ejercicio;
            }
        }

        return new EjercicioVO();
    }

    private void validarVersion(Long versionEsperada, EjercicioVO ejercicio) {
        if (versionEsperada != null && ejercicio.getId() != null && !versionEsperada.equals(ejercicio.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private EjercicioResponseDto toResponse(EjercicioVO ejercicio) {
        EjercicioResponseDto response = new EjercicioResponseDto();
        response.setId(String.valueOf(ejercicio.getId()));
        response.setIdEjercicio(String.valueOf(ejercicio.getId()));
        response.setClientId(ejercicio.getClientId());
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setObservaciones(ejercicio.getObservaciones());
        response.setGrupoMuscular(ejercicio.getGrupoMuscular());
        response.setPatronMovimiento(ejercicio.getPatronMovimiento());
        response.setEquipamiento(ejercicio.getEquipamiento());
        response.setSeriesPlanificadas(ejercicio.getSeriesPlanificadas());
        response.setRepeticionesPlanificadas(ejercicio.getRepeticionesPlanificadas());
        response.setPesoPlanificado(ejercicio.getPesoPlanificado());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        response.setCreatedAt(ejercicio.getCreatedAt());
        response.setUpdatedAt(ejercicio.getUpdatedAt());
        response.setVersion(ejercicio.getVersion());
        return response;
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizar(String valor) {
        return valor.trim();
    }

    private String normalizarNullable(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
