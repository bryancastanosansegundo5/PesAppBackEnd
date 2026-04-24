package com.pesapp.pesapp.usuarios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.security.JwtService;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
import com.pesapp.pesapp.usuarios.model.dto.ActualizarPerfilUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.AuthSessionDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.DisponibilidadUsernameResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.RefreshTokenRepository;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private UsuarioServiceImpl usuarioService;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-change-me-test-secret-change-me");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 900_000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "pesapp-test");
        ReflectionTestUtils.invokeMethod(jwtService, "validarConfiguracion");

        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);
        ReflectionTestUtils.setField(usuarioService, "refreshTokenExpiration", 2_592_000_000L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeCrearUsuarioConUsernameYSinEmail() {
        RegistroUsuarioRequestDto request = new RegistroUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("bryan");
        request.setEmail(null);
        request.setPassword("Secret123");

        when(usuarioRepository.existsByUsernameIgnoreCase("bryan")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hash");
        when(usuarioRepository.save(any(UsuarioVO.class))).thenAnswer(invocation -> {
            UsuarioVO usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        AuthSessionDto sesion = usuarioService.registrar(request);

        ArgumentCaptor<UsuarioVO> captor = ArgumentCaptor.forClass(UsuarioVO.class);
        verify(usuarioRepository).save(captor.capture());
        UsuarioVO guardado = captor.getValue();
        assertThat(guardado.getUsername()).isEqualTo("bryan");
        assertThat(guardado.getEmail()).isNull();
        assertThat(guardado.getRol()).isEqualTo(RolUsuario.USUARIO);
        assertThat(sesion.getResponse().getUsuario().getUsername()).isEqualTo("bryan");
        assertThat(sesion.getResponse().getUsuario().getEmail()).isNull();
    }

    @Test
    void debeRechazarUsernameDuplicado() {
        RegistroUsuarioRequestDto request = new RegistroUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("bryan");
        request.setPassword("Secret123");

        UsuarioVO existente = new UsuarioVO();
        existente.setId(99L);
        existente.setUsername("bryan");
        when(usuarioRepository.existsByUsernameIgnoreCase("bryan")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existe un usuario registrado con ese nombre de usuario");
    }

    @Test
    void debeAceptarEmailNullEnCreacionAdmin() {
        CrearUsuarioAdminRequestDto request = new CrearUsuarioAdminRequestDto();
        request.setNombre("Admin");
        request.setUsername("admin");
        request.setEmail(null);
        request.setPassword("Secret123");
        request.setRol(RolUsuario.ADMIN);

        when(usuarioRepository.existsByUsernameIgnoreCase("admin")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hash");
        when(usuarioRepository.save(any(UsuarioVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioService.crearUsuarioDesdeAdmin(request);

        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getEmail()).isNull();
    }

    @Test
    void debeHacerLoginCorrectoPorUsername() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("bryan");
        request.setPassword("Secret123");

        User principal = new User("bryan", "hash", java.util.List.of(() -> "ROLE_USUARIO"));
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(7L);
        usuario.setNombre("Bryan");
        usuario.setUsername("bryan");
        usuario.setEmail(null);
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setActivo(true);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));

        AuthSessionDto sesion = usuarioService.login(request);

        assertThat(sesion.getResponse().getUsuario().getUsername()).isEqualTo("bryan");
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("bryan", "Secret123"));
    }

    @Test
    void debeObtenerPerfilDelUsuarioAutenticado() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));

        var perfil = usuarioService.obtenerPerfil();

        assertThat(perfil.getId()).isEqualTo(1L);
        assertThat(perfil.getNombre()).isEqualTo("Bryan");
        assertThat(perfil.getUsername()).isEqualTo("bryan");
        assertThat(perfil.getEmail()).isEqualTo("bryan@pesapp.local");
    }

    @Test
    void debeActualizarNombreSinCambiarUsername() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan Garcia");
        request.setUsername("bryan");
        request.setEmail("bryan@pesapp.local");

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("bryan", 1L)).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("bryan@pesapp.local", 1L)).thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(UsuarioVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var actualizado = usuarioService.actualizarPerfil(request);

        assertThat(actualizado.getNombre()).isEqualTo("Bryan Garcia");
        assertThat(actualizado.getUsername()).isEqualTo("bryan");
        assertThat(actualizado.getEmail()).isEqualTo("bryan@pesapp.local");
    }

    @Test
    void debeActualizarUsernameAUnoLibre() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("Nuevo.User");
        request.setEmail("bryan@pesapp.local");

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("nuevo.user", 1L)).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("bryan@pesapp.local", 1L)).thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(UsuarioVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var actualizado = usuarioService.actualizarPerfil(request);

        assertThat(actualizado.getUsername()).isEqualTo("nuevo.user");
    }

    @Test
    void debeRechazarUsernameDuplicadoAlActualizarPerfil() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("otro");
        request.setEmail("bryan@pesapp.local");

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("otro", 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizarPerfil(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existe un usuario registrado con ese nombre de usuario");
    }

    @Test
    void debeActualizarPerfilSinEmail() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("bryan");
        request.setEmail("");

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("bryan", 1L)).thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(UsuarioVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var actualizado = usuarioService.actualizarPerfil(request);

        assertThat(actualizado.getEmail()).isNull();
        verify(usuarioRepository, never()).existsByEmailIgnoreCaseAndIdNot(any(), any());
    }

    @Test
    void debeActualizarPerfilConEmailNull() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("bryan");
        request.setEmail(null);

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("bryan", 1L)).thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(UsuarioVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var actualizado = usuarioService.actualizarPerfil(request);

        assertThat(actualizado.getEmail()).isNull();
    }

    @Test
    void debeRechazarEmailDuplicadoAlActualizarPerfil() {
        autenticarComo("bryan");
        UsuarioVO usuario = crearUsuario(1L, "Bryan", "bryan", "bryan@pesapp.local");
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("bryan");
        request.setEmail("duplicado@pesapp.local");

        when(usuarioRepository.findByUsernameIgnoreCase("bryan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByUsernameIgnoreCaseAndIdNot("bryan", 1L)).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("duplicado@pesapp.local", 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizarPerfil(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existe un usuario registrado con ese email");
    }

    @Test
    void debeInformarDisponibilidadDeUsername() {
        when(usuarioRepository.existsByUsernameIgnoreCase("libre")).thenReturn(false);
        when(usuarioRepository.existsByUsernameIgnoreCase("ocupado")).thenReturn(true);

        DisponibilidadUsernameResponseDto disponible = usuarioService.comprobarDisponibilidadUsername("libre");
        DisponibilidadUsernameResponseDto noDisponible = usuarioService.comprobarDisponibilidadUsername("ocupado");

        assertThat(disponible.isDisponible()).isTrue();
        assertThat(noDisponible.isDisponible()).isFalse();
    }

    private void autenticarComo(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, java.util.List.of()));
    }

    private UsuarioVO crearUsuario(Long id, String nombre, String username, String email) {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setActivo(true);
        usuario.setCreatedAt(LocalDateTime.of(2026, 4, 20, 10, 0));
        usuario.setUpdatedAt(LocalDateTime.of(2026, 4, 20, 10, 0));
        return usuario;
    }
}
