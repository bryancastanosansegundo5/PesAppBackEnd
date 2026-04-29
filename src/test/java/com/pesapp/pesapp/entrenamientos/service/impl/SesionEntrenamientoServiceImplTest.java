package com.pesapp.pesapp.entrenamientos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaSesionEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.SesionEntrenamientoRepository;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SesionEntrenamientoServiceImplTest {

    @Mock
    private SesionEntrenamientoRepository sesionEntrenamientoRepository;

    @Mock
    private PlantillaEjercicioRepository plantillaEjercicioRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SesionEntrenamientoServiceImpl sesionEntrenamientoService;

    private UsuarioVO usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioVO();
        usuario.setId(9L);
        usuario.setUsername("bryan");
        lenient().when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        lenient().when(usuarioRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(usuario));
    }

    @Test
    void debeDevolverIdPersistidoEnListadoAunqueExistaClientId() {
        PlantillaSesionEntrenamientoVO sesion = crearSesion(42L, "sesion-local-42");
        when(sesionEntrenamientoRepository.findAllByUsuario_IdOrderByNombreAsc(9L)).thenReturn(List.of(sesion));

        PlantillaSesionEntrenamientoResponseDto response = sesionEntrenamientoService.obtenerTodas().get(0);

        assertThat(response.getId()).isEqualTo("42");
        assertThat(response.getIdSesion()).isEqualTo("42");
        assertThat(response.getClientId()).isEqualTo("sesion-local-42");
    }

    @Test
    void debeTraducirErrorDeIntegridadAlEliminar() {
        PlantillaSesionEntrenamientoVO sesion = crearSesion(42L, "sesion-local-42");
        when(sesionEntrenamientoRepository.findByIdAndUsuario_Id(42L, 9L)).thenReturn(Optional.of(sesion));
        doThrow(new DataIntegrityViolationException("fk_registros_entrenamiento"))
                .when(sesionEntrenamientoRepository)
                .flush();

        assertThatThrownBy(() -> sesionEntrenamientoService.eliminar(42L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("No se puede eliminar la sesion porque tiene entrenamientos asociados");

        verify(sesionEntrenamientoRepository).delete(sesion);
    }

    @Test
    void debeDevolverSesionExistenteSiLaConstraintUnicaEvitaUnDuplicadoConcurrente() {
        PlantillaSesionEntrenamientoRequestDto request = new PlantillaSesionEntrenamientoRequestDto();
        request.setClientId("sesion-local-42");
        request.setNombreSesion("Push");

        PlantillaSesionEntrenamientoVO existente = crearSesion(42L, "sesion-local-42");

        when(sesionEntrenamientoRepository.findFirstByIdFrontendAndUsuario_IdOrderByIdDesc("sesion-local-42", 9L))
                .thenReturn(Optional.empty(), Optional.of(existente));
        doThrow(new DataIntegrityViolationException("uq_plantillas_sesion_usuario_id_frontend"))
                .when(sesionEntrenamientoRepository)
                .saveAndFlush(any(PlantillaSesionEntrenamientoVO.class));

        PlantillaSesionEntrenamientoResponseDto response = sesionEntrenamientoService.crear(request);

        assertThat(response.getId()).isEqualTo("42");
        assertThat(response.getClientId()).isEqualTo("sesion-local-42");
    }

    private PlantillaSesionEntrenamientoVO crearSesion(Long id, String clientId) {
        PlantillaSesionEntrenamientoVO sesion = new PlantillaSesionEntrenamientoVO();
        sesion.setId(id);
        sesion.setIdFrontend(clientId);
        sesion.setNombre("Push");
        sesion.setUsuario(usuario);
        return sesion;
    }
}
