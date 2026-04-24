package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroSerieRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroSerieResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroSerieVO;
import com.pesapp.pesapp.entrenamientos.repository.EntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntrenamientoServiceImpl implements EntrenamientoService {

    private final EntrenamientoRepository entrenamientoRepository;
    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final PlantillaEjercicioRepository plantillaEjercicioRepository;
    private final RegistroEjercicioRepository registroEjercicioRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public RegistroEntrenamientoResponseDto guardarEntrenamientoFinalizado(RegistroEntrenamientoRequestDto request) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        RegistroEntrenamientoVO entrenamiento = new RegistroEntrenamientoVO();
        entrenamiento.setPlantillaSesion(buscarPlantillaSesionOpcional(request.getPlantillaSesionId(), usuario.getId()));
        entrenamiento.setUsuario(usuario);
        entrenamiento.setNombreSesion(normalizar(request.getNombreSesion()));
        entrenamiento.setFechaInicio(request.getFechaInicio() == null ? LocalDateTime.now() : request.getFechaInicio());
        entrenamiento.setFechaFinalizacion(
                request.getFechaFinalizacion() == null ? LocalDateTime.now() : request.getFechaFinalizacion());

        validarFechas(entrenamiento);
        request.getEjercicios().forEach(ejercicioRequest -> entrenamiento.addEjercicio(toEntity(ejercicioRequest)));

        return toResponse(entrenamientoRepository.save(entrenamiento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroEntrenamientoResponseDto> obtenerHistorico() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return entrenamientoRepository.findAllByUsuario_IdOrderByFechaFinalizacionDescFechaInicioDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroEntrenamientoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarEntrenamiento(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroEjercicioResponseDto> obtenerUltimoRegistroEjercicio(Long plantillaEjercicioId) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        if (!plantillaEjercicioRepository.existsByIdAndPlantillaSesion_Usuario_Id(plantillaEjercicioId, usuario.getId())) {
            throw new EntityNotFoundException("No existe el ejercicio de plantilla con id " + plantillaEjercicioId);
        }

        return registroEjercicioRepository
                .findFirstByPlantillaEjercicio_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
                        plantillaEjercicioId,
                        usuario.getId())
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroEntrenamientoResponseDto> obtenerUltimoRegistroSesion(Long plantillaSesionId) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        if (sesionEntrenamientoRepository.findByIdAndUsuario_Id(plantillaSesionId, usuario.getId()).isEmpty()) {
            throw new EntityNotFoundException("No existe la sesion de entrenamiento con id " + plantillaSesionId);
        }

        return entrenamientoRepository
                .findFirstByPlantillaSesion_IdAndUsuario_IdOrderByFechaFinalizacionDescFechaInicioDescIdDesc(
                        plantillaSesionId,
                        usuario.getId())
                .map(this::toResponse);
    }

    private RegistroEntrenamientoVO buscarEntrenamiento(Long id) {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return entrenamientoRepository.findByIdAndUsuario_Id(id, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("No existe el entrenamiento con id " + id));
    }

    private PlantillaSesionEntrenamientoVO buscarPlantillaSesionOpcional(Long plantillaSesionId, Long usuarioId) {
        if (plantillaSesionId == null) {
            return null;
        }

        return sesionEntrenamientoRepository.findByIdAndUsuario_Id(plantillaSesionId, usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe la sesion de entrenamiento con id " + plantillaSesionId));
    }

    private PlantillaEjercicioVO buscarPlantillaEjercicioOpcional(Long plantillaEjercicioId) {
        if (plantillaEjercicioId == null) {
            return null;
        }

        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        return plantillaEjercicioRepository.findByIdAndPlantillaSesion_Usuario_Id(plantillaEjercicioId, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe el ejercicio de plantilla con id " + plantillaEjercicioId));
    }

    private RegistroEjercicioVO toEntity(RegistroEjercicioRequestDto request) {
        validarEjercicio(request);

        RegistroEjercicioVO ejercicio = new RegistroEjercicioVO();
        ejercicio.setPlantillaEjercicio(buscarPlantillaEjercicioOpcional(request.getPlantillaEjercicioId()));
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setSeriesBase(request.getSeriesBase());
        ejercicio.setRepeticionesBase(request.getRepeticionesBase());
        ejercicio.setPesoBase(request.getPesoBase());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
        ejercicio.setOmitido(request.isOmitido());

        if (!request.isOmitido()) {
            request.getSeriesRealizadas().forEach(serieRequest -> ejercicio.addSerieRealizada(toEntity(serieRequest)));
        }

        return ejercicio;
    }

    private RegistroSerieVO toEntity(RegistroSerieRequestDto request) {
        RegistroSerieVO serie = new RegistroSerieVO();
        serie.setNumeroSerie(request.getNumeroSerie());
        serie.setRepeticiones(request.getRepeticiones());
        serie.setPeso(request.getPeso());
        serie.setOrden(request.getOrden());
        return serie;
    }

    private void validarEjercicio(RegistroEjercicioRequestDto request) {
        if (!request.isOmitido() && request.getSeriesRealizadas().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ejercicio " + request.getNombre() + " necesita series realizadas o debe marcarse como omitido");
        }
    }

    private void validarFechas(RegistroEntrenamientoVO entrenamiento) {
        if (entrenamiento.getFechaFinalizacion().isBefore(entrenamiento.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de finalizacion no puede ser anterior a la fecha de inicio");
        }
    }

    private RegistroEntrenamientoResponseDto toResponse(RegistroEntrenamientoVO entrenamiento) {
        RegistroEntrenamientoResponseDto response = new RegistroEntrenamientoResponseDto();
        response.setId(entrenamiento.getId());
        response.setPlantillaSesionId(entrenamiento.getPlantillaSesion() == null
                ? null
                : entrenamiento.getPlantillaSesion().getId());
        response.setUsuarioId(entrenamiento.getUsuario().getId());
        response.setNombreSesion(entrenamiento.getNombreSesion());
        response.setFechaInicio(entrenamiento.getFechaInicio());
        response.setFechaFinalizacion(entrenamiento.getFechaFinalizacion());
        response.setEjercicios(entrenamiento.getEjercicios().stream().map(this::toResponse).toList());
        response.setCreatedAt(entrenamiento.getCreatedAt());
        response.setUpdatedAt(entrenamiento.getUpdatedAt());
        return response;
    }

    private RegistroEjercicioResponseDto toResponse(RegistroEjercicioVO ejercicio) {
        RegistroEjercicioResponseDto response = new RegistroEjercicioResponseDto();
        response.setId(ejercicio.getId());
        response.setRegistroEntrenamientoId(ejercicio.getRegistroEntrenamiento().getId());
        response.setPlantillaEjercicioId(ejercicio.getPlantillaEjercicio() == null
                ? null
                : ejercicio.getPlantillaEjercicio().getId());
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setSeriesBase(ejercicio.getSeriesBase());
        response.setRepeticionesBase(ejercicio.getRepeticionesBase());
        response.setPesoBase(ejercicio.getPesoBase());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        response.setOmitido(ejercicio.isOmitido());
        response.setSeriesRealizadas(ejercicio.getSeriesRealizadas().stream()
                .sorted(Comparator.comparing(RegistroSerieVO::getOrden).thenComparing(RegistroSerieVO::getId))
                .map(this::toResponse)
                .toList());
        response.setCreatedAt(ejercicio.getCreatedAt());
        response.setUpdatedAt(ejercicio.getUpdatedAt());
        return response;
    }

    private RegistroSerieResponseDto toResponse(RegistroSerieVO serie) {
        RegistroSerieResponseDto response = new RegistroSerieResponseDto();
        response.setId(serie.getId());
        response.setRegistroEjercicioId(serie.getRegistroEjercicio().getId());
        response.setNumeroSerie(serie.getNumeroSerie());
        response.setRepeticiones(serie.getRepeticiones());
        response.setPeso(serie.getPeso());
        response.setOrden(serie.getOrden());
        response.setCreatedAt(serie.getCreatedAt());
        response.setUpdatedAt(serie.getUpdatedAt());
        return response;
    }

    private String normalizar(String valor) {
        return valor.trim();
    }

    private String normalizarNullable(String valor) {
        return valor == null ? null : valor.trim();
    }
}
