package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.service.SesionEntrenamientoService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SesionEntrenamientoServiceImpl implements SesionEntrenamientoService {

    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final UsuarioService usuarioService;

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
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        PlantillaSesionEntrenamientoVO sesion = new PlantillaSesionEntrenamientoVO();
        sesion.setNombre(normalizar(request.getNombre()));
        sesion.setUsuario(usuario);
        request.getEjercicios().forEach(ejercicioRequest -> sesion.addEjercicio(toEntity(ejercicioRequest)));

        return toResponse(sesionEntrenamientoRepository.save(sesion));
    }

    @Override
    @Transactional
    public PlantillaSesionEntrenamientoResponseDto actualizar(Long id, PlantillaSesionEntrenamientoRequestDto request) {
        PlantillaSesionEntrenamientoVO sesion = buscarSesion(id);
        sesion.setNombre(normalizar(request.getNombre()));
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

        Set<Long> idsRecibidos = new HashSet<>();
        for (PlantillaEjercicioRequestDto ejercicioRequest : ejerciciosRequest) {
            if (ejercicioRequest.getId() == null) {
                sesion.addEjercicio(toEntity(ejercicioRequest));
                continue;
            }

            PlantillaEjercicioVO ejercicio = ejerciciosExistentes.get(ejercicioRequest.getId());
            if (ejercicio == null) {
                throw new IllegalArgumentException("El ejercicio con id " + ejercicioRequest.getId()
                        + " no pertenece a la sesion " + sesion.getId());
            }

            idsRecibidos.add(ejercicioRequest.getId());
            copiarCampos(ejercicioRequest, ejercicio);
        }

        sesion.getEjercicios().removeIf(ejercicio -> ejercicio.getId() != null && !idsRecibidos.contains(ejercicio.getId()));
    }

    private PlantillaEjercicioVO toEntity(PlantillaEjercicioRequestDto request) {
        PlantillaEjercicioVO ejercicio = new PlantillaEjercicioVO();
        copiarCampos(request, ejercicio);
        return ejercicio;
    }

    private void copiarCampos(PlantillaEjercicioRequestDto request, PlantillaEjercicioVO ejercicio) {
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setSeriesBase(request.getSeriesBase());
        ejercicio.setRepeticionesBase(request.getRepeticionesBase());
        ejercicio.setPesoBase(request.getPesoBase());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
    }

    private PlantillaSesionEntrenamientoResponseDto toResponse(PlantillaSesionEntrenamientoVO sesion) {
        PlantillaSesionEntrenamientoResponseDto response = new PlantillaSesionEntrenamientoResponseDto();
        response.setId(sesion.getId());
        response.setNombre(sesion.getNombre());
        response.setCreatedAt(sesion.getCreatedAt());
        response.setUpdatedAt(sesion.getUpdatedAt());
        response.setEjercicios(sesion.getEjercicios().stream().map(this::toResponse).toList());
        return response;
    }

    private PlantillaEjercicioResponseDto toResponse(PlantillaEjercicioVO ejercicio) {
        PlantillaEjercicioResponseDto response = new PlantillaEjercicioResponseDto();
        response.setId(ejercicio.getId());
        response.setPlantillaSesionId(ejercicio.getPlantillaSesion().getId());
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setSeriesBase(ejercicio.getSeriesBase());
        response.setRepeticionesBase(ejercicio.getRepeticionesBase());
        response.setPesoBase(ejercicio.getPesoBase());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        response.setCreatedAt(ejercicio.getCreatedAt());
        response.setUpdatedAt(ejercicio.getUpdatedAt());
        return response;
    }

    private String normalizar(String valor) {
        return valor.trim();
    }

    private String normalizarNullable(String valor) {
        return valor == null ? null : valor.trim();
    }
}
