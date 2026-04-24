package com.pesapp.pesapp.entrenamientos.controller;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import com.pesapp.pesapp.entrenamientos.service.SesionEntrenamientoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sesiones-entrenamiento")
public class SesionEntrenamientoController {

    private final SesionEntrenamientoService sesionEntrenamientoService;
    private final EntrenamientoService entrenamientoService;

    @GetMapping
    public ResponseEntity<List<PlantillaSesionEntrenamientoResponseDto>> obtenerTodas() {
        return ResponseEntity.ok(sesionEntrenamientoService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaSesionEntrenamientoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sesionEntrenamientoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PlantillaSesionEntrenamientoResponseDto> crear(
            @Valid @RequestBody PlantillaSesionEntrenamientoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionEntrenamientoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantillaSesionEntrenamientoResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlantillaSesionEntrenamientoRequestDto request) {
        return ResponseEntity.ok(sesionEntrenamientoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sesionEntrenamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ultimo-registro")
    public ResponseEntity<RegistroEntrenamientoResponseDto> obtenerUltimoRegistroSesion(@PathVariable Long id) {
        return entrenamientoService.obtenerUltimoRegistroSesion(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
