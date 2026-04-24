package com.pesapp.pesapp.usuarios.controller;

import com.pesapp.pesapp.usuarios.model.dto.CambiarEstadoUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarRolUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.ActualizarPerfilUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.DisponibilidadUsernameResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> crearUsuario(
            @Valid @RequestBody CrearUsuarioAdminRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuarioDesdeAdmin(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> obtenerMiPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfil());
    }

    @PatchMapping("/me")
    public ResponseEntity<UsuarioResponseDto> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilUsuarioRequestDto request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(request));
    }

    @GetMapping("/disponibilidad-username")
    public ResponseEntity<DisponibilidadUsernameResponseDto> comprobarDisponibilidadUsername(
            @RequestParam String username) {
        return ResponseEntity.ok(usuarioService.comprobarDisponibilidadUsername(username));
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponseDto> cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody CambiarRolUsuarioRequestDto request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDto> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoUsuarioRequestDto request) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolUsuario>> obtenerRoles() {
        return ResponseEntity.ok(List.of(RolUsuario.values()));
    }
}
