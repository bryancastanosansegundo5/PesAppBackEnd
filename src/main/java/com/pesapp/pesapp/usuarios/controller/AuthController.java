package com.pesapp.pesapp.usuarios.controller;

import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.AuthSessionDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.LogoutResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.security.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthCookieService authCookieService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDto> registrar(@Valid @RequestBody RegistroUsuarioRequestDto request) {
        AuthSessionDto sesion = usuarioService.registrar(request);
        HttpHeaders headers = buildAuthHeaders(sesion);
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(sesion.getResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthSessionDto sesion = usuarioService.login(request);
        return ResponseEntity.ok().headers(buildAuthHeaders(sesion)).body(sesion.getResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(HttpServletRequest request) {
        String refreshToken = authCookieService.extraerRefreshToken(request);
        AuthSessionDto sesion = usuarioService.refrescarSesion(refreshToken);
        return ResponseEntity.ok().headers(buildAuthHeaders(sesion)).body(sesion.getResponse());
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> me() {
        return ResponseEntity.ok(usuarioService.obtenerPerfil());
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(HttpServletRequest request) {
        String refreshToken = authCookieService.extraerRefreshToken(request);
        LogoutResponseDto response = usuarioService.logout(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        authCookieService.limpiarCookiesAutenticacion(headers);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    private HttpHeaders buildAuthHeaders(AuthSessionDto sesion) {
        HttpHeaders headers = new HttpHeaders();
        authCookieService.anadirCookiesAutenticacion(
                headers,
                sesion.getAccessToken(),
                Duration.ofMillis(sesion.getResponse().getAccessTokenExpiresIn()),
                sesion.getRefreshToken(),
                Duration.ofMillis(sesion.getResponse().getRefreshTokenExpiresIn()));
        return headers;
    }
}
