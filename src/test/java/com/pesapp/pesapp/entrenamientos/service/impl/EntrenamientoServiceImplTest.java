package com.pesapp.pesapp.entrenamientos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioRequestDto;
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
        when(usuarioRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(usuario));
        lenient().when(entrenamientoRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void debeGuardarEntrenoOfflineConEjercicioDeCatalogoSinUsarIdEjercicioLocalComoPlantilla() {
        EjercicioVO catalogo = new EjercicioVO();
        catalogo.setId(7L);

        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-1", 9L))
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
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-2", 9L))
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

        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-1", 9L))
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
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-1", 9L))
                .thenReturn(Optional.empty());
        when(ejercicioRepository.findByIdAndUsuario_Id(7L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestCatalogo()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("catalogoEjercicioId 7 no existe");
    }

    @Test
    void debeFallarSiPlantillaEjercicioIdNoExiste() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-3", 9L))
                .thenReturn(Optional.empty());
        when(plantillaEjercicioRepository.findByIdAndPlantillaSesion_Usuario_Id(123L, 9L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestConPlantilla("123")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("plantillaEjercicioId 123 no existe");
    }

    @Test
    void debeFallarSiIdSesionNoExiste() {
        when(entrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("entreno-offline-4", 9L))
                .thenReturn(Optional.empty());
        when(sesionEntrenamientoRepository.findByIdAndUsuario_Id(45L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrenamientoService.guardarEntrenamientoFinalizado(crearRequestConSesion("45")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("idSesion 45 no existe");
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

    private EjercicioVO crearCatalogo(Long id) {
        EjercicioVO catalogo = new EjercicioVO();
        catalogo.setId(id);
        return catalogo;
    }

}
