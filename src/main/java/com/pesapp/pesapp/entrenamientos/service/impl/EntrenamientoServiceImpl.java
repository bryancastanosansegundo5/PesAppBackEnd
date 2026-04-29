package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroSerieRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroSerieResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroSerieVO;
import com.pesapp.pesapp.entrenamientos.repository.EntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntrenamientoServiceImpl implements EntrenamientoService {

    private final EntrenamientoRepository entrenamientoRepository;
    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final PlantillaEjercicioRepository plantillaEjercicioRepository;
    private final RegistroEjercicioRepository registroEjercicioRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public RegistroEntrenamientoResponseDto guardarEntrenamientoFinalizado(RegistroEntrenamientoRequestDto request) {
        UsuarioVO usuario = obtenerUsuarioContexto();
        RegistroEntrenamientoVO entrenamiento = buscarEntrenamientoParaUpsert(request, usuario);
        if (entrenamiento.getId() == null) {
            entrenamiento.setUsuario(usuario);
        }

        validarVersion(request.getVersion(), entrenamiento);
        entrenamiento.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getId())));
        entrenamiento.setPlantillaSesion(buscarPlantillaSesionOpcional(request.getIdSesion(), usuario.getId()));
        entrenamiento.setNombreSesion(normalizar(request.getNombreSesion()));
        entrenamiento.setFechaInicio(request.getFechaInicio() == null ? LocalDateTime.now() : request.getFechaInicio());
        entrenamiento.setFechaFinalizacion(
                request.getFechaFin() == null ? LocalDateTime.now() : request.getFechaFin());

        validarFechas(entrenamiento);
        entrenamiento.getEjercicios().clear();
        request.getEjercicios().forEach(ejercicioRequest -> entrenamiento.addEjercicio(toEntity(ejercicioRequest)));

        return toResponse(guardarEntrenamientoIdempotente(entrenamiento, request, usuario.getId()));
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
        boolean existeCatalogo = ejercicioRepository.existsByIdAndUsuario_Id(plantillaEjercicioId, usuario.getId());
        boolean existeLegacy =
                plantillaEjercicioRepository.existsByIdAndPlantillaSesion_Usuario_Id(plantillaEjercicioId, usuario.getId());

        if (!existeCatalogo && !existeLegacy) {
            throw new EntityNotFoundException("No existe el ejercicio de plantilla con id " + plantillaEjercicioId);
        }

        Optional<RegistroEjercicioVO> ultimoRegistro = Optional.empty();
        if (existeCatalogo) {
            ultimoRegistro = registroEjercicioRepository
                    .findFirstByEjercicioCatalogo_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
                            plantillaEjercicioId, usuario.getId());
        }

        if (ultimoRegistro.isEmpty() && existeLegacy) {
            ultimoRegistro = registroEjercicioRepository
                    .findFirstByPlantillaEjercicio_IdAndRegistroEntrenamiento_Usuario_IdAndOmitidoFalseOrderByRegistroEntrenamiento_FechaFinalizacionDescIdDesc(
                            plantillaEjercicioId, usuario.getId());
        }

        return ultimoRegistro.map(this::toResponse);
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

    private PlantillaSesionEntrenamientoVO buscarPlantillaSesionOpcional(String plantillaSesionId, Long usuarioId) {
        if (plantillaSesionId == null || plantillaSesionId.isBlank()) {
            return null;
        }

        Long plantillaSesionNumerica = parseId(plantillaSesionId);
        if (plantillaSesionNumerica == null) {
            return null;
        }

        return sesionEntrenamientoRepository.findByIdAndUsuario_Id(plantillaSesionNumerica, usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("idSesion " + plantillaSesionId + " no existe"));
    }

    private RegistroEntrenamientoVO guardarEntrenamientoIdempotente(
            RegistroEntrenamientoVO entrenamiento,
            RegistroEntrenamientoRequestDto request,
            Long usuarioId) {
        try {
            return entrenamientoRepository.saveAndFlush(entrenamiento);
        } catch (DataIntegrityViolationException exception) {
            String clientId = normalizarNullable(request.getClientId());
            if (clientId != null) {
                RegistroEntrenamientoVO existente = entrenamientoRepository
                        .findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(clientId, usuarioId)
                        .orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            if (entrenamiento.getId() != null) {
                RegistroEntrenamientoVO existente =
                        entrenamientoRepository.findByIdAndUsuario_Id(entrenamiento.getId(), usuarioId).orElse(null);
                if (existente != null) {
                    return existente;
                }
            }

            throw exception;
        }
    }

    private PlantillaEjercicioVO buscarPlantillaEjercicioOpcional(String plantillaEjercicioId) {
        if (plantillaEjercicioId == null || plantillaEjercicioId.isBlank()) {
            return null;
        }

        UsuarioVO usuario = obtenerUsuarioContexto();
        Long plantillaEjercicioNumerico = parseId(plantillaEjercicioId);
        if (plantillaEjercicioNumerico == null) {
            return null;
        }

        return plantillaEjercicioRepository
                .findByIdAndPlantillaSesion_Usuario_Id(plantillaEjercicioNumerico, usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("plantillaEjercicioId " + plantillaEjercicioId + " no existe"));
    }

    private RegistroEjercicioVO toEntity(RegistroEjercicioRequestDto request) {
        validarEjercicio(request);
        PlantillaEjercicioVO plantillaEjercicio = buscarPlantillaEjercicioOpcional(request.getPlantillaEjercicioId());
        EjercicioVO ejercicioCatalogo = buscarEjercicioCatalogoOpcional(request.getCatalogoEjercicioId(), plantillaEjercicio);

        RegistroEjercicioVO ejercicio = new RegistroEjercicioVO();
        ejercicio.setIdFrontend(normalizarNullable(primerValorConTexto(request.getClientId(), request.getIdEjercicio())));
        ejercicio.setPlantillaEjercicio(plantillaEjercicio);
        ejercicio.setEjercicioCatalogo(ejercicioCatalogo);
        ejercicio.setNombre(normalizar(request.getNombre()));
        ejercicio.setDescripcion(normalizarNullable(request.getDescripcion()));
        ejercicio.setGrupoMuscular(normalizarNullable(request.getGrupoMuscular()));
        ejercicio.setPatronMovimiento(normalizarNullable(request.getPatronMovimiento()));
        ejercicio.setEquipamiento(normalizarNullable(request.getEquipamiento()));
        ejercicio.setSeriesBase(request.getSeriesPlanificadas());
        ejercicio.setRepeticionesBase(request.getRepeticionesPlanificadas());
        ejercicio.setPesoBase(request.getPesoPlanificado());
        ejercicio.setAlturaBanco(request.getAlturaBanco());
        ejercicio.setAgarre(normalizarNullable(request.getAgarre()));
        ejercicio.setCompletado(request.isCompletado());
        ejercicio.setOmitido(request.isOmitido());

        if (!request.isOmitido()) {
            for (int i = 0; i < request.getSeriesRealizadas().size(); i++) {
                ejercicio.addSerieRealizada(toEntity(request.getSeriesRealizadas().get(i), i + 1));
            }
        }

        return ejercicio;
    }

    private EjercicioVO buscarEjercicioCatalogoOpcional(String ejercicioId, PlantillaEjercicioVO plantillaEjercicio) {
        if (ejercicioId != null && !ejercicioId.isBlank()) {
            UsuarioVO usuario = obtenerUsuarioContexto();
            Long ejercicioNumerico = parseId(ejercicioId);
            if (ejercicioNumerico == null) {
                throw new EntityNotFoundException("catalogoEjercicioId " + ejercicioId + " no existe");
            }

            return ejercicioRepository.findByIdAndUsuario_Id(ejercicioNumerico, usuario.getId())
                    .orElseThrow(() -> new EntityNotFoundException("catalogoEjercicioId " + ejercicioId + " no existe"));
        }

        return plantillaEjercicio == null ? null : plantillaEjercicio.getEjercicioCatalogo();
    }

    private RegistroSerieVO toEntity(RegistroSerieRequestDto request, int orden) {
        RegistroSerieVO serie = new RegistroSerieVO();
        serie.setIdFrontend(normalizarNullable(request.getId()));
        serie.setNumeroSerie(request.getNumeroSerie());
        serie.setRepeticiones(request.getRepeticiones());
        serie.setPeso(request.getPeso());
        serie.setOrden(orden);
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

    private RegistroEntrenamientoVO buscarEntrenamientoParaUpsert(
            RegistroEntrenamientoRequestDto request,
            UsuarioVO usuario) {
        String clientId = normalizarNullable(request.getClientId());
        if (clientId != null) {
            RegistroEntrenamientoVO entrenamiento = entrenamientoRepository
                    .findFirstByIdFrontendAndUsuario_IdOrderByIdDesc(clientId, usuario.getId())
                    .orElse(null);
            if (entrenamiento != null) {
                return entrenamiento;
            }
        }

        return new RegistroEntrenamientoVO();
    }

    private void validarVersion(Long versionEsperada, RegistroEntrenamientoVO entrenamiento) {
        if (versionEsperada != null
                && entrenamiento.getId() != null
                && !versionEsperada.equals(entrenamiento.getVersion())) {
            throw new OptimisticLockException("La version enviada no coincide con la version actual del recurso");
        }
    }

    private RegistroEntrenamientoResponseDto toResponse(RegistroEntrenamientoVO entrenamiento) {
        RegistroEntrenamientoResponseDto response = new RegistroEntrenamientoResponseDto();
        response.setId(textoConPreferencia(entrenamiento.getIdFrontend(), entrenamiento.getId()));
        response.setIdSesion(entrenamiento.getPlantillaSesion() == null
                ? null
                : textoConPreferencia(
                        entrenamiento.getPlantillaSesion().getIdFrontend(),
                        entrenamiento.getPlantillaSesion().getId()));
        response.setClientId(entrenamiento.getIdFrontend());
        response.setNombreSesion(entrenamiento.getNombreSesion());
        response.setFechaInicio(entrenamiento.getFechaInicio());
        response.setFechaFin(entrenamiento.getFechaFinalizacion());
        response.setCreatedAt(entrenamiento.getCreatedAt());
        response.setUpdatedAt(entrenamiento.getUpdatedAt());
        response.setVersion(entrenamiento.getVersion());
        response.setEjercicios(entrenamiento.getEjercicios().stream().map(this::toResponse).toList());
        return response;
    }

    private RegistroEjercicioResponseDto toResponse(RegistroEjercicioVO ejercicio) {
        RegistroEjercicioResponseDto response = new RegistroEjercicioResponseDto();
        response.setIdEjercicio(ejercicio.getPlantillaEjercicio() == null
                ? textoConPreferencia(ejercicio.getIdFrontend(), ejercicio.getId())
                : textoConPreferencia(
                        ejercicio.getPlantillaEjercicio().getIdFrontend(),
                        ejercicio.getPlantillaEjercicio().getId()));
        response.setClientId(ejercicio.getIdFrontend());
        response.setPlantillaEjercicioId(ejercicio.getPlantillaEjercicio() == null
                ? null
                : toTexto(ejercicio.getPlantillaEjercicio().getId()));
        response.setCatalogoEjercicioId(obtenerCatalogoEjercicioId(ejercicio));
        response.setNombre(ejercicio.getNombre());
        response.setDescripcion(ejercicio.getDescripcion());
        response.setGrupoMuscular(ejercicio.getGrupoMuscular());
        response.setPatronMovimiento(ejercicio.getPatronMovimiento());
        response.setEquipamiento(ejercicio.getEquipamiento());
        response.setSeriesPlanificadas(ejercicio.getSeriesBase());
        response.setRepeticionesPlanificadas(ejercicio.getRepeticionesBase());
        response.setPesoPlanificado(ejercicio.getPesoBase());
        response.setAlturaBanco(ejercicio.getAlturaBanco());
        response.setAgarre(ejercicio.getAgarre());
        response.setCompletado(ejercicio.isCompletado());
        response.setOmitido(ejercicio.isOmitido());
        response.setCreatedAt(ejercicio.getCreatedAt());
        response.setUpdatedAt(ejercicio.getUpdatedAt());
        response.setVersion(ejercicio.getVersion());
        response.setSeriesRealizadas(ejercicio.getSeriesRealizadas().stream()
                .sorted(Comparator.comparing(RegistroSerieVO::getOrden).thenComparing(RegistroSerieVO::getId))
                .map(this::toResponse)
                .toList());
        return response;
    }

    private String obtenerCatalogoEjercicioId(RegistroEjercicioVO ejercicio) {
        if (ejercicio.getEjercicioCatalogo() != null) {
            return toTexto(ejercicio.getEjercicioCatalogo().getId());
        }

        if (ejercicio.getPlantillaEjercicio() != null && ejercicio.getPlantillaEjercicio().getEjercicioCatalogo() != null) {
            return toTexto(ejercicio.getPlantillaEjercicio().getEjercicioCatalogo().getId());
        }

        return null;
    }

    private RegistroSerieResponseDto toResponse(RegistroSerieVO serie) {
        RegistroSerieResponseDto response = new RegistroSerieResponseDto();
        response.setId(textoConPreferencia(serie.getIdFrontend(), serie.getId()));
        response.setNumeroSerie(serie.getNumeroSerie());
        response.setRepeticiones(serie.getRepeticiones());
        response.setPeso(serie.getPeso());
        response.setClientId(serie.getIdFrontend());
        response.setCreatedAt(serie.getCreatedAt());
        response.setUpdatedAt(serie.getUpdatedAt());
        response.setVersion(serie.getVersion());
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

    private String primerValorConTexto(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
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
