package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.service.SesionEntrenamientoService;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SesionEntrenamientoServiceImpl implements SesionEntrenamientoService {

    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final PlantillaEjercicioRepository plantillaEjercicioRepository;
    private final EjercicioRepository ejercicioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaSesionEntrenamientoResponseDto> obtenerTodas() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return sesionEntrenamientoRepository.findAllByUsuario_IdOrderByNombreAsc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaSesionEntrenamientoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarSesion(id));
    }

    @Override
    @Transactional
    public PlantillaSesionEntrenamientoResponseDto crear(PlantillaSesionEntrenamientoRequestDto request) {
        UsuarioVO usuario = obtenerUsuarioContexto();
        PlantillaSesionEntrenamientoVO sesion = buscarSesionParaUpsert(request, usuario);

        if (sesion.getId() == null) {
            sesion.setUsuario(usuario);
        }

        validarVersion(request.getVersion(), sesion);
        sesion.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getId(), request.getIdSesion())));
        sesion.setNombre(normalizar(request.getNombreSesion()));
        sesion.setObservaciones(normalizarNullable(request.getObservaciones()));
        actualizarEjercicios(sesion, request.getEjercicios());

        return toResponse(guardarSesionIdempotente(sesion, request, usuario.getId()));
    }

    @Override
    @Transactional
    public PlantillaSesionEntrenamientoResponseDto actualizar(Long id, PlantillaSesionEntrenamientoRequestDto request) {
        PlantillaSesionEntrenamientoVO sesion = buscarSesion(id);
        validarVersion(request.getVersion(), sesion);
        if (sesion.getIdFrontend() == null) {
            sesion.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getId(), request.getIdSesion())));
        }
        sesion.setNombre(normalizar(request.getNombreSesion()));
        sesion.setObservaciones(normalizarNullable(request.getObservaciones()));
        actualizarEjercicios(sesion, request.getEjercicios());

        return toResponse(guardarSesionIdempotente(sesion, request, sesion.getUsuario().getId()));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PlantillaSesionEntrenamientoVO sesion = buscarSesion(id);
        try {
            sesionEntrenamientoRepository.delete(sesion);
            sesionEntrenamientoRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("No se puede eliminar la sesion porque tiene entrenamientos asociados");
        }
    }

    private PlantillaSesionEntrenamientoVO buscarSesion(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return sesionEntrenamientoRepository.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("No existe la sesion de entrenamiento con id " + id));
    }

    private PlantillaSesionEntrenamientoVO guardarSesionIdempotente(
            PlantillaSesionEntrenamientoVO sesion,
            PlantillaSesionEntrenamientoRequestDto request,
            Long usuarioId) {
        try {
            return sesionEntrenamientoRepository.saveAndFlush(sesion);
        } catch (DataIntegrityViolationException exception) {
            String idFrontend =
                    normalizarNullable(primerValorConTexto(request.getClientId(), request.getId(), request.getIdSesion()));
            if (idFrontend != null) {
                PlantillaSesionEntrenamientoVO existente = sesionEntrenamientoRepository
                        .findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(idFrontend, usuarioId)
                        .orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            if (sesion.getId() != null) {
                PlantillaSesionEntrenamientoVO existente =
                        sesionEntrenamientoRepository.findByIdAndUsuario_Id(sesion.getId(), usuarioId).orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            throw exception;
        }
    }

    private void actualizarEjercicios(
            PlantillaSesionEntrenamientoVO sesion,
            List<PlantillaEjercicioRequestDto> ejerciciosRequest) {

        Map<Long, PlantillaEjercicioVO> ejerciciosExistentes = new HashMap<>();
        sesion.getEjercicios().stream()
                .filter(ejercicio -> ejercicio.getId() != null)
                .forEach(ejercicio -> ejerciciosExistentes.put(ejercicio.getId(), ejercicio));

        Map<String, PlantillaEjercicioVO> ejerciciosExistentesPorFrontend = new HashMap<>();
        sesion.getEjercicios().stream()
                .filter(ejercicio -> ejercicio.getIdFrontend() != null && !ejercicio.getIdFrontend().isBlank())
                .forEach(ejercicio -> ejerciciosExistentesPorFrontend.put(ejercicio.getIdFrontend().trim(), ejercicio));

        Set<Long> idsRecibidos = new HashSet<>();
        for (PlantillaEjercicioRequestDto ejercicioRequest : ejerciciosRequest) {
            PlantillaEjercicioVO ejercicio = buscarEjercicioExistenteParaUpsert(
                    sesion, ejercicioRequest, ejerciciosExistentes, ejerciciosExistentesPorFrontend);
            if (ejercicio == null) {
                ejercicio = toEntity(ejercicioRequest);
                sesion.addEjercicio(ejercicio);
            }

            if (ejercicio.getId() != null) {
                idsRecibidos.add(ejercicio.getId());
            }
            copiarCampos(ejercicioRequest, ejercicio);
        }

        sesion.getEjercicios().removeIf(ejercicio -> ejercicio.getId() != null && !idsRecibidos.contains(ejercicio.getId()));
    }

    private PlantillaEjercicioVO toEntity(PlantillaEjercicioRequestDto request) {
        PlantillaEjercicioVO ejercicio = new PlantillaEjercicioVO();
        ejercicio.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getIdEjercicio())));
        copiarCampos(request, ejercicio);
        return ejercicio;
    }

    private PlantillaSesionEntrenamientoVO buscarSesionParaUpsert(
            PlantillaSesionEntrenamientoRequestDto request,
            UsuarioVO usuario) {
        String idFrontend = normalizarNullable(primerValorConTexto(request.getClientId(), request.getId(), request.getIdSesion()));
        Long idNumerico = parseId(idFrontend);

        if (idNumerico != null) {
            PlantillaSesionEntrenamientoVO sesion =
                    sesionEntrenamientoRepository.findByIdAndUsuario_Id(idNumerico, usuario.getId()).orElse(null);
            if (sesion != null) {
                return sesion;
            }
        }

        if (idFrontend != null) {
            PlantillaSesionEntrenamientoVO sesion = sesionEntrenamientoRepository
                    .findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(idFrontend, usuario.getId())
                    .orElse(null);
            if (sesion != null) {
                return sesion;
            }
        }

        return new PlantillaSesionEntrenamientoVO();
    }

    private PlantillaEjercicioVO buscarEjercicioExistenteParaUpsert(
            PlantillaSesionEntrenamientoVO sesion,
            PlantillaEjercicioRequestDto request,
            Map<Long, PlantillaEjercicioVO> ejerciciosExistentes,
            Map<String, PlantillaEjercicioVO> ejerciciosExistentesPorFrontend) {
        String idEjercicio = normalizarNullable(primerValorConTexto(request.getClientId(), request.getIdEjercicio()));
        Long ejercicioId = parseId(idEjercicio);

        if (ejercicioId != null) {
            PlantillaEjercicioVO ejercicio = ejerciciosExistentes.get(ejercicioId);
            if (ejercicio != null) {
                return ejercicio;
            }

            if (sesion.getId() != null) {
                ejercicio = plantillaEjercicioRepository
                        .findByIdAndPlantillaSesion_Usuario_Id(ejercicioId, sesion.getUsuario().getId())
                        .filter(item -> item.getPlantillaSesion().getId().equals(sesion.getId()))
                        .orElse(null);
                if (ejercicio != null) {
                    return ejercicio;
                }
            }
        }

        if (idEjercicio != null) {
            PlantillaEjercicioVO ejercicio = ejerciciosExistentesPorFrontend.get(idEjercicio);
            if (ejercicio != null) {
                return ejercicio;
            }

            if (sesion.getId() != null) {
                return plantillaEjercicioRepository
                        .findFirstByIdFrontendAndPlantillaSesion_IdOrderByIdDesc(idEjercicio, sesion.getId())
                        .orElse(null);
            }
        }

        return null;
    }

    private void copiarCampos(PlantillaEjercicioRequestDto request, PlantillaEjercicioVO ejercicio) {
        EjercicioVO ejercicioCatalogo = buscarEjercicioCatalogoOpcional(request.getCatalogoEjercicioId());
        validarVersion(request.getVersion(), ejercicio);
        ejercicio.setEjercicioCatalogo(ejercicioCatalogo);
        ejercicio.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getIdEjercicio())));
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setObservaciones(normalizarNullable(request.getObservaciones()));
        ejercicio.setGrupoMuscular(normalizarNullable(request.getGrupoMuscular()));
        ejercicio.setPatronMovimiento(normalizarNullable(request.getPatronMovimiento()));
        ejercicio.setEquipamiento(normalizarNullable(request.getEquipamiento()));
        ejercicio.setSeriesBase(request.getSeriesPlanificadas());
        ejercicio.setRepeticionesBase(request.getRepeticionesPlanificadas());
        ejercicio.setPesoBase(request.getPesoPlanificado());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
    }

    private PlantillaSesionEntrenamientoResponseDto toResponse(PlantillaSesionEntrenamientoVO sesion) {
        PlantillaSesionEntrenamientoResponseDto response = new PlantillaSesionEntrenamientoResponseDto();
        response.setId(toTexto(sesion.getId()));
        response.setIdSesion(toTexto(sesion.getId()));
        response.setClientId(sesion.getIdFrontend());
        response.setNombreSesion(sesion.getNombre());
        response.setObservaciones(sesion.getObservaciones());
        response.setCreatedAt(sesion.getCreatedAt());
        response.setUpdatedAt(sesion.getUpdatedAt());
        response.setVersion(sesion.getVersion());
        response.setEjercicios(sesion.getEjercicios().stream().map(this::toResponse).toList());
        return response;
    }

    private PlantillaEjercicioResponseDto toResponse(PlantillaEjercicioVO ejercicio) {
        PlantillaEjercicioResponseDto response = new PlantillaEjercicioResponseDto();
        response.setIdEjercicio(textoConPreferencia(ejercicio.getIdFrontend(), ejercicio.getId()));
        response.setClientId(ejercicio.getIdFrontend());
        response.setCatalogoEjercicioId(
                ejercicio.getEjercicioCatalogo() == null ? null : toTexto(ejercicio.getEjercicioCatalogo().getId()));
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setObservaciones(ejercicio.getObservaciones());
        response.setGrupoMuscular(ejercicio.getGrupoMuscular());
        response.setPatronMovimiento(ejercicio.getPatronMovimiento());
        response.setEquipamiento(ejercicio.getEquipamiento());
        response.setSeriesPlanificadas(ejercicio.getSeriesBase());
        response.setRepeticionesPlanificadas(ejercicio.getRepeticionesBase());
        response.setPesoPlanificado(ejercicio.getPesoBase());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        response.setCreatedAt(ejercicio.getCreatedAt());
        response.setUpdatedAt(ejercicio.getUpdatedAt());
        response.setVersion(ejercicio.getVersion());
        return response;
    }

    private void validarVersion(Long versionEsperada, com.pesapp.pesapp.entrenamientos.model.vo.AuditoriaVO entidad) {
        if (versionEsperada != null
                && entidad instanceof com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO sesion
                && sesion.getId() != null
                && !versionEsperada.equals(sesion.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }

        if (versionEsperada != null
                && entidad instanceof com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO ejercicio
                && ejercicio.getId() != null
                && !versionEsperada.equals(ejercicio.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private EjercicioVO buscarEjercicioCatalogoOpcional(String ejercicioId) {
        if (ejercicioId == null || ejercicioId.isBlank()) {
            return null;
        }

        UsuarioVO usuario = obtenerUsuarioContexto();
        Long ejercicioNumerico = parseId(ejercicioId);
        if (ejercicioNumerico == null) {
            throw new EntityNotFoundException("No existe el ejercicio del catalogo con id " + ejercicioId);
        }

        return ejercicioRepository.findByIdAndUsuario_Id(ejercicioNumerico, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe el ejercicio del catalogo con id " + ejercicioId));
    }

    private UsuarioVO obtenerUsuarioContexto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return usuarioService.obtenerUsuarioAutenticado();
        }

        return usuarioRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario disponible para guardar datos"));
    }

    private Long parseId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toTexto(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    private String textoConPreferencia(String idFrontend, Long idInterno) {
        return (idFrontend != null && !idFrontend.isBlank()) ? idFrontend : toTexto(idInterno);
    }

    private String primerValorConTexto(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        return null;
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
