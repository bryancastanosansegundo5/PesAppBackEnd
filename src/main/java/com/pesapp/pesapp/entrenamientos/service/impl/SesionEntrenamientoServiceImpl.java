package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.service.SesionEntrenamientoService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SesionEntrenamientoServiceImpl implements SesionEntrenamientoService {

    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final PlantillaEjercicioRepository plantillaEjercicioRepository;
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

        sesion.setIdFrontend(normalizarNullable(primerValorConTexto(request.getId(), request.getIdSesion())));
        sesion.setNombre(normalizar(request.getNombreSesion()));
        actualizarEjercicios(sesion, request.getEjercicios());

        return toResponse(sesionEntrenamientoRepository.save(sesion));
    }

    @Override
    @Transactional
    public PlantillaSesionEntrenamientoResponseDto actualizar(Long id, PlantillaSesionEntrenamientoRequestDto request) {
        PlantillaSesionEntrenamientoVO sesion = buscarSesion(id);
        sesion.setNombre(normalizar(request.getNombreSesion()));
        actualizarEjercicios(sesion, request.getEjercicios());

        return toResponse(sesion);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PlantillaSesionEntrenamientoVO sesion = buscarSesion(id);
        sesionEntrenamientoRepository.delete(sesion);
    }

    private PlantillaSesionEntrenamientoVO buscarSesion(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return sesionEntrenamientoRepository.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("No existe la sesion de entrenamiento con id " + id));
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
        ejercicio.setIdFrontend(normalizarNullable(request.getIdEjercicio()));
        copiarCampos(request, ejercicio);
        return ejercicio;
    }

    private PlantillaSesionEntrenamientoVO buscarSesionParaUpsert(
            PlantillaSesionEntrenamientoRequestDto request,
            UsuarioVO usuario) {
        String idFrontend = normalizarNullable(primerValorConTexto(request.getId(), request.getIdSesion()));
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
        String idEjercicio = normalizarNullable(request.getIdEjercicio());
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
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setSeriesBase(request.getSeriesPlanificadas());
        ejercicio.setRepeticionesBase(request.getRepeticionesPlanificadas());
        ejercicio.setPesoBase(request.getPesoPlanificado());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
    }

    private PlantillaSesionEntrenamientoResponseDto toResponse(PlantillaSesionEntrenamientoVO sesion) {
        PlantillaSesionEntrenamientoResponseDto response = new PlantillaSesionEntrenamientoResponseDto();
        response.setId(textoConPreferencia(sesion.getIdFrontend(), sesion.getId()));
        response.setIdSesion(textoConPreferencia(sesion.getIdFrontend(), sesion.getId()));
        response.setNombreSesion(sesion.getNombre());
        response.setEjercicios(sesion.getEjercicios().stream().map(this::toResponse).toList());
        return response;
    }

    private PlantillaEjercicioResponseDto toResponse(PlantillaEjercicioVO ejercicio) {
        PlantillaEjercicioResponseDto response = new PlantillaEjercicioResponseDto();
        response.setIdEjercicio(textoConPreferencia(ejercicio.getIdFrontend(), ejercicio.getId()));
        response.setNombre(ejercicio.getNombre());
        response.setSeriesPlanificadas(ejercicio.getSeriesBase());
        response.setRepeticionesPlanificadas(ejercicio.getRepeticionesBase());
        response.setPesoPlanificado(ejercicio.getPesoBase());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        return response;
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

    private String primerValorConTexto(String primerValor, String segundoValor) {
        if (primerValor != null && !primerValor.isBlank()) {
            return primerValor.trim();
        }
        if (segundoValor != null && !segundoValor.isBlank()) {
            return segundoValor.trim();
        }
        return null;
    }

    private String normalizar(String valor) {
        return valor.trim();
    }

    private String normalizarNullable(String valor) {
        return valor == null ? null : valor.trim();
    }
}
