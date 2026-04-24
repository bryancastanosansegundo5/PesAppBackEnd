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
import java.util.List;
import lombok.RequiredArgsConstructor;
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
        EjercicioVO ejercicio = new EjercicioVO();
        ejercicio.setUsuario(usuario);
        copiarCampos(request, ejercicio);
        return toResponse(ejercicioRepository.save(ejercicio));
    }

    @Override
    @Transactional
    public EjercicioResponseDto actualizar(Long id, EjercicioRequestDto request) {
        EjercicioVO ejercicio = buscarEjercicio(id);
        copiarCampos(request, ejercicio);
        return toResponse(ejercicio);
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

    private void copiarCampos(EjercicioRequestDto request, EjercicioVO ejercicio) {
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setGrupoMuscular(normalizarNullable(request.getGrupoMuscular()));
        ejercicio.setPatronMovimiento(normalizarNullable(request.getPatronMovimiento()));
        ejercicio.setEquipamiento(normalizarNullable(request.getEquipamiento()));
        ejercicio.setSeriesPlanificadas(request.getSeriesPlanificadas());
        ejercicio.setRepeticionesPlanificadas(request.getRepeticionesPlanificadas());
        ejercicio.setPesoPlanificado(request.getPesoPlanificado());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
    }

    private EjercicioResponseDto toResponse(EjercicioVO ejercicio) {
        EjercicioResponseDto response = new EjercicioResponseDto();
        response.setId(String.valueOf(ejercicio.getId()));
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setGrupoMuscular(ejercicio.getGrupoMuscular());
        response.setPatronMovimiento(ejercicio.getPatronMovimiento());
        response.setEquipamiento(ejercicio.getEquipamiento());
        response.setSeriesPlanificadas(ejercicio.getSeriesPlanificadas());
        response.setRepeticionesPlanificadas(ejercicio.getRepeticionesPlanificadas());
        response.setPesoPlanificado(ejercicio.getPesoPlanificado());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        return response;
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
