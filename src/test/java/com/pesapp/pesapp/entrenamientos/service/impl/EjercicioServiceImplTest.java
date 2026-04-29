package com.pesapp.pesapp.entrenamientos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.repository.EjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.PlantillaEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class EjercicioServiceImplTest {

    @Mock
    private EjercicioRepository ejercicioRepository;

    @Mock
    private PlantillaEjercicioRepository plantillaEjercicioRepository;

    @Mock
    private RegistroEjercicioRepository registroEjercicioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private EjercicioServiceImpl ejercicioService;

    private UsuarioVO usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioVO();
        usuario.setId(9L);
        lenient().when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        lenient().when(ejercicioRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void debeDevolverEjercicioExistenteSiLaConstraintUnicaEvitaUnDuplicadoConcurrente() {
        EjercicioRequestDto request = new EjercicioRequestDto();
        request.setClientId("ejercicio-offline-1");
        request.setNombre("Press militar");
        request.setSeriesPlanificadas(4);
        request.setRepeticionesPlanificadas(8);

        EjercicioVO existente = new EjercicioVO();
        existente.setId(21L);
        existente.setClientId("ejercicio-offline-1");
        existente.setUsuario(usuario);
        existente.setNombre("Press militar");
        existente.setSeriesPlanificadas(4);
        existente.setRepeticionesPlanificadas(8);

        when(ejercicioRepository.findFirstByClientIdAndUsuario_IdOrderByIdDesc("ejercicio-offline-1", 9L))
                .thenReturn(Optional.empty(), Optional.of(existente));
        when(ejercicioRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("uq_ejercicios_usuario_client_id"));

        EjercicioResponseDto response = ejercicioService.crear(request);

        assertThat(response.getId()).isEqualTo("21");
        assertThat(response.getClientId()).isEqualTo("ejercicio-offline-1");
    }
}
