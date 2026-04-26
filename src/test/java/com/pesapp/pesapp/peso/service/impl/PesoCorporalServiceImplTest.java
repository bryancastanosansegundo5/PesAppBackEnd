package com.pesapp.pesapp.peso.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import com.pesapp.pesapp.peso.model.vo.PesoCorporalVO;
import com.pesapp.pesapp.peso.repository.PesoCorporalRepository;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PesoCorporalServiceImplTest {

    @Mock
    private PesoCorporalRepository pesoCorporalRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PesoCorporalServiceImpl pesoCorporalService;

    @Test
    void debeExponerLaVersionPersistidaSinTransformarlaEnElHistorico() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);
        existente.setVersion(0L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findAllByUsuario_IdOrderByFechaDescCreatedAtDesc(1L))
                .thenReturn(List.of(existente));

        List<PesoCorporalResponseDto> response = pesoCorporalService.obtenerHistorico();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getVersion()).isZero();
    }

    @Test
    void debeMantenerFechaRegistroExistenteAlActualizarPesoHoyPorClientId() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);
        PesoHoyRequestDto request = new PesoHoyRequestDto();
        request.setPeso(new BigDecimal("77.70"));
        request.setHoraRegistro("14:22");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26");
        request.setVersion(1L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findFirstByClientIdAndUsuario_IdOrderByIdDesc("peso-2026-04-26", 1L))
                .thenReturn(Optional.of(existente));
        when(pesoCorporalRepository.saveAndFlush(any(PesoCorporalVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = pesoCorporalService.guardarPesoHoy(request);

        ArgumentCaptor<PesoCorporalVO> captor = ArgumentCaptor.forClass(PesoCorporalVO.class);
        verify(pesoCorporalRepository).saveAndFlush(captor.capture());
        PesoCorporalVO guardado = captor.getValue();
        assertThat(guardado.getFechaRegistro()).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(guardado.getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 26, 14, 22));
        assertThat(guardado.getHoraRegistro()).isEqualTo("14:22");
        assertThat(guardado.isHoraManual()).isTrue();
        assertThat(response.getFechaRegistro()).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(response.getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 26, 14, 22));
    }

    @Test
    void debeCrearNuevoPesoHoyAunqueYaExistaOtroEnLaMismaFechaSiElClientIdEsDistinto() {
        UsuarioVO usuario = crearUsuario();
        PesoHoyRequestDto request = new PesoHoyRequestDto();
        request.setPeso(new BigDecimal("77.90"));
        request.setHoraRegistro("20:10");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26-tarde");

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findFirstByClientIdAndUsuario_IdOrderByIdDesc("peso-2026-04-26-tarde", 1L))
                .thenReturn(Optional.empty());
        when(pesoCorporalRepository.saveAndFlush(any(PesoCorporalVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PesoCorporalResponseDto response = pesoCorporalService.guardarPesoHoy(request);

        ArgumentCaptor<PesoCorporalVO> captor = ArgumentCaptor.forClass(PesoCorporalVO.class);
        verify(pesoCorporalRepository).saveAndFlush(captor.capture());
        PesoCorporalVO guardado = captor.getValue();
        assertThat(guardado.getId()).isNull();
        assertThat(guardado.getClientId()).isEqualTo("peso-2026-04-26-tarde");
        assertThat(guardado.getFechaRegistro()).isEqualTo(LocalDate.now());
        assertThat(guardado.getFecha()).isEqualTo(LocalDateTime.of(LocalDate.now(), java.time.LocalTime.of(20, 10)));
        assertThat(response.getClientId()).isEqualTo("peso-2026-04-26-tarde");
        assertThat(response.getHoraRegistro()).isEqualTo("20:10");
    }

    @Test
    void debeCrearNuevoPesoPorFechaAunqueYaExistaOtroEseMismoDiaSiNoComparteClientId() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);
        com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto request =
                new com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto();
        request.setPeso(new BigDecimal("77.10"));
        request.setFechaRegistro(LocalDate.of(2026, 4, 26));
        request.setHoraRegistro("21:05");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26-noche");

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findFirstByClientIdAndUsuario_IdOrderByIdDesc("peso-2026-04-26-noche", 1L))
                .thenReturn(Optional.empty());
        when(pesoCorporalRepository.saveAndFlush(any(PesoCorporalVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PesoCorporalResponseDto response = pesoCorporalService.guardar(request);

        ArgumentCaptor<PesoCorporalVO> captor = ArgumentCaptor.forClass(PesoCorporalVO.class);
        verify(pesoCorporalRepository).saveAndFlush(captor.capture());
        PesoCorporalVO guardado = captor.getValue();
        assertThat(guardado).isNotSameAs(existente);
        assertThat(guardado.getId()).isNull();
        assertThat(guardado.getFechaRegistro()).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(guardado.getHoraRegistro()).isEqualTo("21:05");
        assertThat(response.getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 26, 21, 5));
    }

    @Test
    void debeRechazarPesoHoyCuandoLaVersionNoCoincideConLaPersistida() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);
        existente.setVersion(0L);
        PesoHoyRequestDto request = new PesoHoyRequestDto();
        request.setPeso(new BigDecimal("77.90"));
        request.setHoraRegistro("14:17");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26");
        request.setVersion(1L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findFirstByClientIdAndUsuario_IdOrderByIdDesc("peso-2026-04-26", 1L))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> pesoCorporalService.guardarPesoHoy(request))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage("La version enviada no coincide con la version actual del recurso");
    }

    private UsuarioVO crearUsuario() {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(1L);
        usuario.setNombre("Bryan");
        usuario.setUsername("bryan");
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setActivo(true);
        return usuario;
    }

    private PesoCorporalVO crearPesoExistente(UsuarioVO usuario) {
        PesoCorporalVO peso = new PesoCorporalVO();
        peso.setId(3L);
        peso.setUsuario(usuario);
        peso.setClientId("peso-2026-04-26");
        peso.setPeso(new BigDecimal("78.40"));
        peso.setFechaRegistro(LocalDate.of(2026, 4, 26));
        peso.setHoraRegistro("14:16");
        peso.setHoraManual(false);
        peso.setFecha(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setCreatedAt(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setUpdatedAt(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setVersion(1L);
        return peso;
    }
}
