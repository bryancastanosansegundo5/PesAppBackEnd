package com.pesapp.pesapp.usuarios.service.impl;

import com.pesapp.pesapp.security.JwtService;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
import com.pesapp.pesapp.usuarios.model.dto.ActualizarPerfilUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.AuthSessionDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarEstadoUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarRolUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.DisponibilidadUsernameResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.LogoutResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.RefreshTokenVO;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.RefreshTokenRepository;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthSessionDto registrar(RegistroUsuarioRequestDto request) {
        String email = normalizarEmail(request.getEmail());
        String username = resolverUsernameDisponible(request.getUsername(), null);
        validarEmailDisponible(email, null);

        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setActivo(true);

        UsuarioVO guardado = usuarioRepository.save(usuario);
        return crearSesionAutenticada(guardado, crearUserDetails(guardado));
    }

    @Override
    @Transactional
    public AuthSessionDto login(LoginRequestDto request) {
        String username = normalizarUsername(request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UsuarioVO usuario = buscarPorUsername(username);

        return crearSesionAutenticada(usuario, userDetails);
    }

    @Override
    @Transactional
    public AuthSessionDto refrescarSesion(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("No se ha proporcionado refresh token");
        }

        RefreshTokenVO tokenPersistido = refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token no valido"));

        if (!tokenPersistido.estaActivo()) {
            throw new BadCredentialsException("Refresh token expirado o revocado");
        }

        tokenPersistido.setRevokedAt(LocalDateTime.now());
        UsuarioVO usuario = tokenPersistido.getUsuario();
        return crearSesionAutenticada(usuario, crearUserDetails(usuario));
    }

    @Override
    @Transactional
    public LogoutResponseDto logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.revocarPorHash(hashToken(refreshToken), LocalDateTime.now());
        }

        SecurityContextHolder.clearContext();
        return new LogoutResponseDto(true, "Sesion cerrada correctamente");
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioVO obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No hay un usuario autenticado");
        }

        return buscarPorUsername(authentication.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPerfil() {
        return toResponse(obtenerUsuarioAutenticado());
    }

    @Override
    @Transactional
    public UsuarioResponseDto actualizarPerfil(ActualizarPerfilUsuarioRequestDto request) {
        UsuarioVO usuario = obtenerUsuarioAutenticado();
        String email = normalizarEmail(request.getEmail());
        String username = normalizarUsername(request.getUsername());
        validarFormatoEmail(email);
        validarUsernameDisponible(username, usuario.getId());
        validarEmailDisponible(email, usuario.getId());

        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);

        return toResponse(usuarioRepository.saveAndFlush(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadUsernameResponseDto comprobarDisponibilidadUsername(String username) {
        String usernameNormalizado = normalizarUsername(username);
        return new DisponibilidadUsernameResponseDto(!usuarioRepository.existsByUsernameIgnoreCase(usernameNormalizado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> obtenerUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UsuarioVO::getId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponseDto crearUsuarioDesdeAdmin(CrearUsuarioAdminRequestDto request) {
        String email = normalizarEmail(request.getEmail());
        String username = resolverUsernameDisponible(request.getUsername(), null);
        validarEmailDisponible(email, null);

        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setActivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDto cambiarRol(Long usuarioId, CambiarRolUsuarioRequestDto request) {
        UsuarioVO usuario = buscarPorId(usuarioId);
        usuario.setRol(request.getRol());
        return toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDto cambiarEstado(Long usuarioId, CambiarEstadoUsuarioRequestDto request) {
        UsuarioVO usuario = buscarPorId(usuarioId);
        usuario.setActivo(request.getActivo());
        return toResponse(usuario);
    }

    private AuthSessionDto crearSesionAutenticada(UsuarioVO usuario, UserDetails userDetails) {
        String accessToken = jwtService.generarToken(userDetails, usuario.getId());
        String refreshTokenPlano = generarRefreshTokenPlano();

        RefreshTokenVO refreshToken = new RefreshTokenVO();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(hashToken(refreshTokenPlano));
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000));
        refreshTokenRepository.save(refreshToken);

        AuthResponseDto response = new AuthResponseDto(
                "Cookie",
                jwtService.getExpirationTime(),
                refreshTokenExpiration,
                true,
                toResponse(usuario));
        return new AuthSessionDto(accessToken, refreshTokenPlano, response);
    }

    private UsuarioVO buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario autenticado"));
    }

    private UsuarioVO buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario con id " + id));
    }

    private UsuarioResponseDto toResponse(UsuarioVO usuario) {
        UsuarioResponseDto response = new UsuarioResponseDto();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.isActivo());
        response.setCreatedAt(usuario.getCreatedAt());
        response.setUpdatedAt(usuario.getUpdatedAt());
        return response;
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String resolverUsernameDisponible(String usernameSolicitado, Long usuarioIdActual) {
        String usernameNormalizado = normalizarUsername(usernameSolicitado);
        validarUsernameDisponible(usernameNormalizado, usuarioIdActual);
        return usernameNormalizado;
    }

    private String normalizarUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        return sanitizarUsername(username.trim());
    }

    private String sanitizarUsername(String value) {
        String sanitizado = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (sanitizado.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no contiene caracteres validos");
        }
        if (sanitizado.length() < 3 || sanitizado.length() > 60) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 60 caracteres");
        }
        return sanitizado;
    }

    private void validarUsernameDisponible(String username, Long usuarioIdActual) {
        boolean duplicado = usuarioIdActual == null
                ? usuarioRepository.existsByUsernameIgnoreCase(username)
                : usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(username, usuarioIdActual);
        if (duplicado) {
            throw new ConflictException("Ya existe un usuario registrado con ese nombre de usuario");
        }
    }

    private void validarEmailDisponible(String email, Long usuarioIdActual) {
        if (email == null) {
            return;
        }

        boolean duplicado = usuarioIdActual == null
                ? usuarioRepository.existsByEmailIgnoreCase(email)
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, usuarioIdActual);
        if (duplicado) {
            throw new ConflictException("Ya existe un usuario registrado con ese email");
        }
    }

    private void validarFormatoEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
    }

    private UserDetails crearUserDetails(UsuarioVO usuario) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .build();
    }

    private String generarRefreshTokenPlano() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se ha podido generar el hash del refresh token", exception);
        }
    }
}
