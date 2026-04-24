package com.pesapp.pesapp.usuarios.service.impl;

import com.pesapp.pesapp.security.JwtService;
import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarEstadoUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarRolUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponseDto registrar(RegistroUsuarioRequestDto request) {
        String email = normalizarEmail(request.getEmail());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }

        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setActivo(true);

        UsuarioVO guardado = usuarioRepository.save(usuario);
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(guardado.getEmail())
                .password(guardado.getPasswordHash())
                .roles(guardado.getRol().name())
                .build();

        return crearAuthResponse(guardado, userDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        String email = normalizarEmail(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UsuarioVO usuario = buscarPorEmail(email);

        return crearAuthResponse(usuario, userDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioVO obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No hay un usuario autenticado");
        }

        return buscarPorEmail(authentication.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPerfil() {
        return toResponse(obtenerUsuarioAutenticado());
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
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }

        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombre(request.getNombre().trim());
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

    private AuthResponseDto crearAuthResponse(UsuarioVO usuario, UserDetails userDetails) {
        String token = jwtService.generarToken(userDetails, usuario.getId());
        return new AuthResponseDto(token, "Bearer", jwtService.getExpirationTime(), toResponse(usuario));
    }

    private UsuarioVO buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
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
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.isActivo());
        response.setCreatedAt(usuario.getCreatedAt());
        response.setUpdatedAt(usuario.getUpdatedAt());
        return response;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
