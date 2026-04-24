package com.pesapp.pesapp.entrenamientos.controller;

import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.EjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEjercicioResponseDto;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import com.pesapp.pesapp.entrenamientos.service.EjercicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService ejercicioService;
    private final EntrenamientoService entrenamientoService;

    @GetMapping
    public ResponseEntity<List<EjercicioResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(ejercicioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EjercicioResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ejercicioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EjercicioResponseDto> crear(@Valid @RequestBody EjercicioRequestDto request) {
        return ResponseEntity.ok(ejercicioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EjercicioResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EjercicioRequestDto request) {
        return ResponseEntity.ok(ejercicioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ejercicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{ejercicioId}/ultimo-registro")
    public ResponseEntity<RegistroEjercicioResponseDto> obtenerUltimoRegistroEjercicio(@PathVariable Long ejercicioId) {
        return entrenamientoService.obtenerUltimoRegistroEjercicio(ejercicioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
