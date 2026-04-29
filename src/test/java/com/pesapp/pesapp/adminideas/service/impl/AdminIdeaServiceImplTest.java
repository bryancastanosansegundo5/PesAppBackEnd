package com.pesapp.pesapp.adminideas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaCreateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaEstadoUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaResponseDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.vo.AdminIdeaVO;
import com.pesapp.pesapp.adminideas.repository.AdminIdeaRepository;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
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
class AdminIdeaServiceImplTest {

    @Mock
    private AdminIdeaRepository adminIdeaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AdminIdeaServiceImpl adminIdeaService;

    @Test
    void debeDevolverIdeasOrdenadasPorUpdatedAtDesc() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO reciente = crearIdea(2L, "idea-2", "Nueva", true, LocalDateTime.of(2026, 4, 29, 11, 30), 1L);
        AdminIdeaVO antigua = crearIdea(1L, "idea-1", "Vieja", false, LocalDateTime.of(2026, 4, 29, 9, 15), 0L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findAllByOrderByUpdatedAtDescIdDesc()).thenReturn(List.of(reciente, antigua));

        List<AdminIdeaResponseDto> response = adminIdeaService.obtenerIdeas();

        assertThat(response).extracting(AdminIdeaResponseDto::getId).containsExactly("2", "1");
        assertThat(response).extracting(AdminIdeaResponseDto::isActivo).containsExactly(true, false);
        assertThat(response).extracting(AdminIdeaResponseDto::getVersion).containsExactly(1L, 0L);
    }

    @Test
    void debeHacerPostIdempotentePorClientIdSinDuplicarRegistros() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO existente = crearIdea(7L, "idea-local-7", "Idea inicial", true, LocalDateTime.of(2026, 4, 29, 10, 0), 3L);
        AdminIdeaCreateRequestDto request = new AdminIdeaCreateRequestDto();
        request.setClientId(" idea-local-7 ");
        request.setTitulo("  Idea editada desde retry ");
        request.setDescripcion("  Descripcion consolidada ");
        request.setCompletada(Boolean.TRUE);
        request.setActivo(Boolean.TRUE);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findByClientId("idea-local-7")).thenReturn(Optional.of(existente));
        when(adminIdeaRepository.saveAndFlush(any(AdminIdeaVO.class))).thenAnswer(invocation -> {
            AdminIdeaVO idea = invocation.getArgument(0);
            idea.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 30));
            idea.setVersion(4L);
            return idea;
        });

        AdminIdeaResponseDto response = adminIdeaService.guardarIdea(request);

        ArgumentCaptor<AdminIdeaVO> captor = ArgumentCaptor.forClass(AdminIdeaVO.class);
        verify(adminIdeaRepository).saveAndFlush(captor.capture());
        AdminIdeaVO guardada = captor.getValue();
        assertThat(guardada.getId()).isEqualTo(7L);
        assertThat(guardada.getClientId()).isEqualTo("idea-local-7");
        assertThat(guardada.getTitulo()).isEqualTo("Idea editada desde retry");
        assertThat(guardada.getDescripcion()).isEqualTo("Descripcion consolidada");
        assertThat(guardada.isCompletada()).isTrue();
        assertThat(guardada.isActivo()).isTrue();
        assertThat(response.getId()).isEqualTo("7");
        assertThat(response.getClientId()).isEqualTo("idea-local-7");
        assertThat(response.isCompletada()).isTrue();
        assertThat(response.isActivo()).isTrue();
        assertThat(response.getVersion()).isEqualTo(4L);
    }

    @Test
    void debeActualizarIdeaSiLaVersionCoincide() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO existente = crearIdea(5L, "idea-local-5", "Pendiente", true, LocalDateTime.of(2026, 4, 29, 10, 0), 2L);
        AdminIdeaUpdateRequestDto request = new AdminIdeaUpdateRequestDto();
        request.setTitulo("  Lista para produccion ");
        request.setDescripcion("  Se ha revisado y validado ");
        request.setCompletada(Boolean.TRUE);
        request.setActivo(Boolean.TRUE);
        request.setVersion(2L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(adminIdeaRepository.saveAndFlush(any(AdminIdeaVO.class))).thenAnswer(invocation -> {
            AdminIdeaVO idea = invocation.getArgument(0);
            idea.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 10, 45));
            idea.setVersion(3L);
            return idea;
        });

        AdminIdeaResponseDto response = adminIdeaService.actualizarIdea(5L, request);

        assertThat(response.getTitulo()).isEqualTo("Lista para produccion");
        assertThat(response.getDescripcion()).isEqualTo("Se ha revisado y validado");
        assertThat(response.isCompletada()).isTrue();
        assertThat(response.isActivo()).isTrue();
        assertThat(response.getVersion()).isEqualTo(3L);
    }

    @Test
    void debeResponderConflictoSiLaVersionNoCoincide() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO existente = crearIdea(5L, "idea-local-5", "Pendiente", true, LocalDateTime.of(2026, 4, 29, 10, 0), 2L);
        AdminIdeaUpdateRequestDto request = new AdminIdeaUpdateRequestDto();
        request.setTitulo("Lista para produccion");
        request.setDescripcion("Se ha revisado y validado");
        request.setCompletada(Boolean.TRUE);
        request.setVersion(1L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> adminIdeaService.actualizarIdea(5L, request))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage("La version enviada no coincide con la version actual del recurso");
    }

    @Test
    void debeResponder404SiLaIdeaNoExisteEnPatch() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaUpdateRequestDto request = new AdminIdeaUpdateRequestDto();
        request.setTitulo("Lista para produccion");
        request.setDescripcion("Se ha revisado y validado");
        request.setCompletada(Boolean.TRUE);
        request.setVersion(1L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminIdeaService.actualizarIdea(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No existe la idea administrativa con id 99");
    }

    @Test
    void debeHacerBorradoLogicoSinEliminarFisicamente() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO existente = crearIdea(8L, "idea-local-8", "Ocultable", true, LocalDateTime.of(2026, 4, 29, 11, 0), 4L);
        AdminIdeaEstadoUpdateRequestDto request = new AdminIdeaEstadoUpdateRequestDto();
        request.setActivo(Boolean.FALSE);
        request.setVersion(4L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(8L)).thenReturn(Optional.of(existente));
        when(adminIdeaRepository.saveAndFlush(any(AdminIdeaVO.class))).thenAnswer(invocation -> {
            AdminIdeaVO idea = invocation.getArgument(0);
            idea.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 11, 5));
            idea.setVersion(5L);
            return idea;
        });

        AdminIdeaResponseDto response = adminIdeaService.actualizarEstado(8L, request);

        verify(adminIdeaRepository).saveAndFlush(argThat(idea -> idea.getId().equals(8L) && !idea.isActivo()));
        assertThat(response.isActivo()).isFalse();
        assertThat(response.getVersion()).isEqualTo(5L);
    }

    @Test
    void debeReactivarIdeaInactiva() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaVO existente = crearIdea(9L, "idea-local-9", "Reactivar", false, LocalDateTime.of(2026, 4, 29, 12, 0), 7L);
        AdminIdeaEstadoUpdateRequestDto request = new AdminIdeaEstadoUpdateRequestDto();
        request.setActivo(Boolean.TRUE);
        request.setVersion(7L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(9L)).thenReturn(Optional.of(existente));
        when(adminIdeaRepository.saveAndFlush(any(AdminIdeaVO.class))).thenAnswer(invocation -> {
            AdminIdeaVO idea = invocation.getArgument(0);
            idea.setUpdatedAt(LocalDateTime.of(2026, 4, 29, 12, 10));
            idea.setVersion(8L);
            return idea;
        });

        AdminIdeaResponseDto response = adminIdeaService.actualizarEstado(9L, request);

        assertThat(response.isActivo()).isTrue();
        assertThat(response.getVersion()).isEqualTo(8L);
    }

    @Test
    void debeResponder404SiLaIdeaNoExisteEnPatchEstado() {
        UsuarioVO admin = crearAdmin();
        AdminIdeaEstadoUpdateRequestDto request = new AdminIdeaEstadoUpdateRequestDto();
        request.setActivo(Boolean.FALSE);
        request.setVersion(2L);

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(admin);
        when(adminIdeaRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminIdeaService.actualizarEstado(88L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No existe la idea administrativa con id 88");
    }

    private UsuarioVO crearAdmin() {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(1L);
        usuario.setNombre("Admin");
        usuario.setUsername("admin");
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setActivo(true);
        return usuario;
    }

    private AdminIdeaVO crearIdea(
            Long id,
            String clientId,
            String titulo,
            boolean activo,
            LocalDateTime updatedAt,
            Long version) {
        AdminIdeaVO idea = new AdminIdeaVO();
        idea.setId(id);
        idea.setClientId(clientId);
        idea.setTitulo(titulo);
        idea.setDescripcion("Descripcion");
        idea.setCompletada(false);
        idea.setActivo(activo);
        idea.setCreatedAt(LocalDateTime.of(2026, 4, 29, 8, 0));
        idea.setUpdatedAt(updatedAt);
        idea.setVersion(version);
        return idea;
    }
}
