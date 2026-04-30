package com.pesapp.pesapp.entrenamientos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoDeleteRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroSerieRequestDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.repository.EntrenamientoRepository;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntrenamientoServiceImplTest {

    @Mock
    private EntrenamientoRepository entrenamientoRepository;

    @Mock
    private SesionEntrenamientoRepository sesionEntrenamientoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @Mock
    private PlantillaEjercicioRepository plantillaEjercicioRepository;

    @Mock
    private RegistroEjercicioRepository registroEjercicioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EntrenamientoServiceImpl entrenamientoService;

    private UsuarioVO usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioVO();
        usuario.setId(9L);
        usuario.setUsername("bryan");
        lenient().when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        lenient().when(usuarioRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(usuario));
        lenient().when(entrenamientoRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void debeGuardarEntrenoOfflineConEjercicioDeCatalogoSinUsarIdEjercicioLocalComoPlantilla() {
        EjercicioVO catalogo = new EjercicioVO();
        catalogo.setId(7L);

        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-1", 9L))
                .thenReturn(Optional.empty());
        when(ejercicioRepository.findByIdAndUsuario_Id(7L, 9L)).thenReturn(Optional.of(catalogo));

        RegistroEntrenamientoResponseDto response =
                entrenamientoService.guardarEntrenamientoFinalizado(crearRequestCatalogo());

        assertThat(response.getClientId()).isEqualTo("entreno-offline-1");
        assertThat(response.getEjercicios()).hasSize(1);
        assertThat(response.getEjercicios().getFirst().getCatalogoEjercicioId()).isEqualTo("7");
        assertThat(response.getEjercicios().getFirst().getPlantillaEjercicioId()).isNull();
        verify(plantillaEjercicioRepository, never()).findByIdAndPlantillaSesion_Usuario_Id(any(), any());
    }

    @Test
    void debeGuardarEntrenoOfflineConEjercicioAdHoc() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-2", 9L))
                .thenReturn(Optional.empty());

        RegistroEntrenamientoResponseDto response =
                entrenamientoService.guardarEntrenamientoFinalizado(crearRequestAdHoc());

        assertThat(response.getEjercicios()).hasSize(1);
        assertThat(response.getEjercicios().getFirst().getNombre()).isEqualTo("Face pull improvisado");
        assertThat(response.getEjercicios().getFirst().getCatalogoEjercicioId()).isNull();
        assertThat(response.getEjercicios().getFirst().getPlantillaEjercicioId()).isNull();
        verify(ejercicioRepository, never()).findByIdAndUsuario_Id(any(), any());
        verify(plantillaEjercicioRepository, never()).findByIdAndPlantillaSesion_Usuario_Id(any(), any());
    }

    @Test
    void debeReutilizarEntrenoExistenteSiLlegaElMismoClientId() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(55L);
        existente.setIdFrontend("entreno-offline-1");
        existente.setUsuario(usuario);
        existente.setNombreSesion("Sesion antigua");

        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-1", 9L))
                .thenReturn(Optional.of(existente));
        when(ejercicioRepository.findByIdAndUsuario_Id(7L, 9L)).thenReturn(Optional.of(crearCatalogo(7L)));

        entrenamientoService.guardarEntrenamientoFinalizado(crearRequestCatalogo());

        ArgumentCaptor<RegistroEntrenamientoVO> captor = ArgumentCaptor.forClass(RegistroEntrenamientoVO.class);
        verify(entrenamientoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(55L);
        assertThat(captor.getValue().getNombreSesion()).isEqualTo("Push offline");
    }

    @Test
    void debeFallarSiCatalogoEjercicioIdNoExiste() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-1", 9L))
                .thenReturn(Optional.empty());
        when(ejercicioRepository.findByIdAndUsuario_Id(7L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestCatalogo()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("catalogoEjercicioId 7 no existe");
    }

    @Test
    void debeFallarSiPlantillaEjercicioIdNoExiste() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-3", 9L))
                .thenReturn(Optional.empty());
        when(plantillaEjercicioRepository.findByIdAndPlantillaSesion_Usuario_Id(123L, 9L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestConPlantilla("123")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("plantillaEjercicioId 123 no existe");
    }

    @Test
    void debeFallarSiIdSesionNoExiste() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entreno-offline-4", 9L))
                .thenReturn(Optional.empty());
        when(sesionEntrenamientoRepository.findByIdAndUsuario_Id(45L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestConSesion("45")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("idSesion 45 no existe");
    }

    @Test
    void debeActualizarEntrenamientoHistoricoReemplazandoEjerciciosYPermitiendoListaVacia() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(123L);
        existente.setIdFrontend("entrenamiento-cliente-1");
        existente.setUsuario(usuario);
        existente.setNombreSesion("Sesion antigua");
        existente.setFechaInicio(LocalDateTime.of(2026, 4, 29, 10, 0));
        existente.setFechaFinalizacion(LocalDateTime.of(2026, 4, 29, 11, 0));
        existente.setVersion(3L);

        when(entrenamientoRepository.findByIdAndUsuario_IdAndDeletedAtIsNull(123L, 9L)).thenReturn(Optional.of(existente));

        RegistroEntrenamientoRequestDto request = crearBaseRequest("entrenamiento-cliente-1");
        request.setId("123");
        request.setVersion(3L);
        request.setEjercicios(List.of());

        RegistroEntrenamientoResponseDto response = entrenamientoService.actualizarEntrenamiento(123L, request);

        assertThat(response.getClientId()).isEqualTo("entrenamiento-cliente-1");
        assertThat(response.getNombreSesion()).isEqualTo("Push offline");
        assertThat(response.getEjercicios()).isEmpty();
        verify(entrenamientoRepository).saveAndFlush(existente);
    }

    @Test
    void debeEliminarEjerciciosPersistidosAntesDeInsertarLosNuevosEnUnaActualizacion() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(123L);
        existente.setIdFrontend("entrenamiento-cliente-1");
        existente.setUsuario(usuario);
        existente.setNombreSesion("Sesion antigua");
        existente.setFechaInicio(LocalDateTime.of(2026, 4, 29, 10, 0));
        existente.setFechaFinalizacion(LocalDateTime.of(2026, 4, 29, 11, 0));
        existente.setVersion(3L);
        existente.addEjercicio(crearEjercicioPersistido("ejercicio-1777566366252-c3d12989385e98"));

        when(entrenamientoRepository.findByIdAndUsuario_IdAndDeletedAtIsNull(123L, 9L)).thenReturn(Optional.of(existente));

        RegistroEntrenamientoRequestDto request = crearBaseRequest("entrenamiento-cliente-1");
        request.setId("123");
        request.setVersion(3L);
        request.setEjercicios(List.of(crearEjercicioCatalogo("ejercicio-1777566366252-c3d12989385e98", "7")));
        when(ejercicioRepository.findByIdAndUsuario_Id(7L, 9L)).thenReturn(Optional.of(crearCatalogo(7L)));

        entrenamientoService.actualizarEntrenamiento(123L, request);

        InOrder inOrder = org.mockito.Mockito.inOrder(registroEjercicioRepository, entrenamientoRepository);
        inOrder.verify(registroEjercicioRepository).deleteAllInBatch(any());
        inOrder.verify(registroEjercicioRepository).flush();
        inOrder.verify(entrenamientoRepository).saveAndFlush(existente);
    }

    @Test
    void debeFallarAlActualizarSiLaVersionNoCoincide() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(123L);
        existente.setIdFrontend("entrenamiento-cliente-1");
        existente.setUsuario(usuario);
        existente.setVersion(5L);
        existente.setFechaInicio(LocalDateTime.of(2026, 4, 29, 10, 0));
        existente.setFechaFinalizacion(LocalDateTime.of(2026, 4, 29, 11, 0));
        existente.setNombreSesion("Sesion antigua");

        when(entrenamientoRepository.findByIdAndUsuario_IdAndDeletedAtIsNull(123L, 9L)).thenReturn(Optional.of(existente));

        RegistroEntrenamientoRequestDto request = crearBaseRequest("entrenamiento-cliente-1");
        request.setId("123");
        request.setVersion(4L);
        request.setEjercicios(List.of());

        assertThatThrownBy(() -> entrenamientoService.actualizarEntrenamiento(123L, request))
                .isInstanceOf(jakarta.persistence.OptimisticLockException.class)
                .hasMessage("La version enviada no coincide con la version actual del recurso");
    }

    @Test
    void debeMarcarComoBorradoEntrenamientoHistorico() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(123L);
        existente.setIdFrontend("entrenamiento-cliente-1");
        existente.setUsuario(usuario);
        existente.setVersion(4L);

        when(entrenamientoRepository.findByIdAndUsuario_Id(123L, 9L)).thenReturn(Optional.of(existente));

        RegistroEntrenamientoDeleteRequestDto request = new RegistroEntrenamientoDeleteRequestDto();
        request.setId("123");
        request.setClientId("entrenamiento-cliente-1");
        request.setVersion(4L);

        entrenamientoService.eliminarEntrenamiento("123", request);

        assertThat(existente.getDeletedAt()).isNotNull();
        verify(entrenamientoRepository).saveAndFlush(existente);
    }

    @Test
    void debePermitirBorrarEntrenamientoUsandoClientIdComoIdentificadorPrincipal() {
        RegistroEntrenamientoVO existente = new RegistroEntrenamientoVO();
        existente.setId(123L);
        existente.setIdFrontend("entrenamiento-cliente-1");
        existente.setUsuario(usuario);
        existente.setVersion(4L);

        when(entrenamientoRepository
                        .findFirstByIdFrontendAndUsuario_IdAndDeletedAtIsNullOrderByIdDesc("entrenamiento-cliente-1", 9L))
                .thenReturn(Optional.of(existente));

        RegistroEntrenamientoDeleteRequestDto request = new RegistroEntrenamientoDeleteRequestDto();
        request.setClientId("entrenamiento-cliente-1");
        request.setVersion(4L);

        entrenamientoService.eliminarEntrenamiento("entrenamiento-cliente-1", request);

        assertThat(existente.getDeletedAt()).isNotNull();
        verify(entrenamientoRepository).saveAndFlush(existente);
    }

    private RegistroEntrenamientoRequestDto crearRequestCatalogo() {
        RegistroEntrenamientoRequestDto request = crearBaseRequest("entreno-offline-1");
        request.setEjercicios(List.of(crearEjercicioCatalogo("ejercicio-1777", "7")));
        return request;
    }

    private RegistroEntrenamientoRequestDto crearRequestAdHoc() {
        RegistroEntrenamientoRequestDto request = crearBaseRequest("entreno-offline-2");
        RegistroEjercicioRequestDto ejercicio = crearEjercicioBase("manual-123", "Face pull improvisado");
        ejercicio.setSeriesRealizadas(List.of(crearSerie(1, 15, "12")));
        request.setEjercicios(List.of(ejercicio));
        return request;
    }

    private RegistroEntrenamientoRequestDto crearRequestConPlantilla(String plantillaEjercicioId) {
        RegistroEntrenamientoRequestDto request = crearBaseRequest("entreno-offline-3");
        RegistroEjercicioRequestDto ejercicio = crearEjercicioBase("hoy-444", "Remo unilateral");
        ejercicio.setPlantillaEjercicioId(plantillaEjercicioId);
        ejercicio.setSeriesRealizadas(List.of(crearSerie(1, 12, "20")));
        request.setEjercicios(List.of(ejercicio));
        return request;
    }

    private RegistroEntrenamientoRequestDto crearRequestConSesion(String idSesion) {
        RegistroEntrenamientoRequestDto request = crearRequestAdHoc();
        request.setClientId("entreno-offline-4");
        request.setIdSesion(idSesion);
        return request;
    }

    private RegistroEntrenamientoRequestDto crearBaseRequest(String clientId) {
        RegistroEntrenamientoRequestDto request = new RegistroEntrenamientoRequestDto();
        request.setId("entrenamiento-local-" + clientId);
        request.setClientId(clientId);
        request.setNombreSesion("Push offline");
        request.setFechaInicio(LocalDateTime.of(2026, 4, 26, 10, 0));
        request.setFechaFin(LocalDateTime.of(2026, 4, 26, 11, 0));
        return request;
    }

    private RegistroEjercicioRequestDto crearEjercicioCatalogo(String idEjercicioLocal, String catalogoEjercicioId) {
        RegistroEjercicioRequestDto ejercicio = crearEjercicioBase(idEjercicioLocal, "Press banca");
        ejercicio.setCatalogoEjercicioId(catalogoEjercicioId);
        ejercicio.setSeriesRealizadas(List.of(crearSerie(1, 8, "80")));
        return ejercicio;
    }

    private RegistroEjercicioRequestDto crearEjercicioBase(String idEjercicioLocal, String nombre) {
        RegistroEjercicioRequestDto ejercicio = new RegistroEjercicioRequestDto();
        ejercicio.setIdEjercicio(idEjercicioLocal);
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion("Descripcion");
        ejercicio.setGrupoMuscular("Pecho");
        ejercicio.setPatronMovimiento("Empuje");
        ejercicio.setEquipamiento("Barra");
        ejercicio.setSeriesPlanificadas(3);
        ejercicio.setRepeticionesPlanificadas(8);
        ejercicio.setPesoPlanificado(new BigDecimal("80"));
        ejercicio.setCompletado(true);
        ejercicio.setOmitido(false);
        return ejercicio;
    }

    private RegistroSerieRequestDto crearSerie(int numeroSerie, int repeticiones, String peso) {
        RegistroSerieRequestDto serie = new RegistroSerieRequestDto();
        serie.setId("serie-" + numeroSerie);
        serie.setNumeroSerie(numeroSerie);
        serie.setRepeticiones(repeticiones);
        serie.setPeso(new BigDecimal(peso));
        return serie;
    }

    private com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO crearEjercicioPersistido(String clientId) {
        com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO ejercicio =
                new com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO();
        ejercicio.setId(77L);
        ejercicio.setIdFrontend(clientId);
        ejercicio.setNombre("Press banca");
        ejercicio.setDescripcion("Descripcion");
        ejercicio.setGrupoMuscular("Pecho");
        ejercicio.setPatronMovimiento("Empuje");
        ejercicio.setEquipamiento("Barra");
        ejercicio.setSeriesBase(3);
        ejercicio.setRepeticionesBase(8);
        ejercicio.setPesoBase(new BigDecimal("80"));
        ejercicio.setCompletado(true);
        ejercicio.setOmitido(false);
        return ejercicio;
    }

    private EjercicioVO crearCatalogo(Long id) {
        EjercicioVO catalogo = new EjercicioVO();
        catalogo.setId(id);
        return catalogo;
    }

}
