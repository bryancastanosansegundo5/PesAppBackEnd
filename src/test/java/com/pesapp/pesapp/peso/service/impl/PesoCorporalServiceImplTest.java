package com.pesapp.pesapp.peso.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import com.pesapp.pesapp.peso.model.vo.PesoCorporalVO;
import com.pesapp.pesapp.peso.repository.PesoCorporalRepository;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;

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
        assertThat(response.get(0).getComentario()).isEqualTo("Peso en ayunas tras descansar bien");
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
        request.setComentario("Nuevo comentario");
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
        assertThat(guardado.getComentario()).isEqualTo("Nuevo comentario");
        assertThat(response.getFechaRegistro()).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(response.getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 26, 14, 22));
        assertThat(response.getComentario()).isEqualTo("Nuevo comentario");
    }

    @Test
    void debeCrearNuevoPesoHoyAunqueYaExistaOtroEnLaMismaFechaSiElClientIdEsDistinto() {
        UsuarioVO usuario = crearUsuario();
        PesoHoyRequestDto request = new PesoHoyRequestDto();
        request.setPeso(new BigDecimal("77.90"));
        request.setHoraRegistro("20:10");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26-tarde");
        request.setComentario(" ");

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
        assertThat(guardado.getComentario()).isNull();
        assertThat(response.getClientId()).isEqualTo("peso-2026-04-26-tarde");
        assertThat(response.getHoraRegistro()).isEqualTo("20:10");
        assertThat(response.getComentario()).isNull();
    }

    @Test
    void debeCrearNuevoPesoPorFechaAunqueYaExistaOtroEseMismoDiaSiNoComparteClientId() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);
        PesoCorporalRequestDto request = new PesoCorporalRequestDto();
        request.setPeso(new BigDecimal("77.10"));
        request.setFechaRegistro(LocalDate.of(2026, 4, 26));
        request.setHoraRegistro("21:05");
        request.setHoraManual(true);
        request.setClientId("peso-2026-04-26-noche");
        request.setComentario("Peso despues de entrenar");

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
        assertThat(guardado.getComentario()).isEqualTo("Peso despues de entrenar");
        assertThat(response.getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 26, 21, 5));
        assertThat(response.getComentario()).isEqualTo("Peso despues de entrenar");
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

    @Test
    void debeEliminarPesoExistenteDelUsuarioAutenticado() {
        UsuarioVO usuario = crearUsuario();
        PesoCorporalVO existente = crearPesoExistente(usuario);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findById(3L)).thenReturn(Optional.of(existente));

        pesoCorporalService.eliminar(3L);

        verify(pesoCorporalRepository).delete(existente);
    }

    @Test
    void debeRechazarEliminarPesoDeOtroUsuarioSiNoEsAdmin() {
        UsuarioVO usuario = crearUsuario();
        UsuarioVO propietario = crearUsuario();
        propietario.setId(2L);
        PesoCorporalVO existente = crearPesoExistente(propietario);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findById(3L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> pesoCorporalService.eliminar(3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No tienes permisos para eliminar este registro de peso");
    }

    @Test
    void debePermitirEliminarPesoDeOtroUsuarioSiEsAdmin() {
        UsuarioVO admin = crearUsuario();
        admin.setRol(RolUsuario.ADMIN);
        UsuarioVO propietario = crearUsuario();
        propietario.setId(2L);
        PesoCorporalVO existente = crearPesoExistente(propietario);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(pesoCorporalRepository.findById(3L)).thenReturn(Optional.of(existente));

        pesoCorporalService.eliminar(3L);

        verify(pesoCorporalRepository).delete(existente);
    }

    @Test
    void debeFallarAlEliminarPesoInexistente() {
        UsuarioVO usuario = crearUsuario();

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(pesoCorporalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pesoCorporalService.eliminar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No existe el peso corporal con id 999");
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
        peso.setComentario("Peso en ayunas tras descansar bien");
        peso.setFecha(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setCreatedAt(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setUpdatedAt(LocalDateTime.of(2026, 4, 26, 14, 16));
        peso.setVersion(1L);
        return peso;
    }
}
