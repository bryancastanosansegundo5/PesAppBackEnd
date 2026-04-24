package com.pesapp.pesapp.usuarios.controller;

import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/registro")
    public ResponseEntity<AuthResponseDto> registrar(@Valid @RequestBody RegistroUsuarioRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> me() {
        return ResponseEntity.ok(usuarioService.obtenerPerfil());
    }
}
