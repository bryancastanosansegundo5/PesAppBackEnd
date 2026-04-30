package com.pesapp.pesapp.entrenamientos.controller;

import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoDeleteRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoRequestDto;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
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
@RequestMapping("/api")
public class EntrenamientoController {

    private final EntrenamientoService entrenamientoService;

    @PostMapping("/entrenamientos")
    public ResponseEntity<RegistroEntrenamientoResponseDto> guardarEntrenamientoFinalizado(
            @Valid @RequestBody RegistroEntrenamientoRequestDto request) {
        return ResponseEntity.ok(entrenamientoService.guardarEntrenamientoFinalizado(request));
    }

    @PutMapping("/entrenamientos/{id}")
    public ResponseEntity<RegistroEntrenamientoResponseDto> actualizarEntrenamiento(
            @PathVariable Long id,
            @Valid @RequestBody RegistroEntrenamientoRequestDto request) {
        return ResponseEntity.ok(entrenamientoService.actualizarEntrenamiento(id, request));
    }

    @DeleteMapping("/entrenamientos/{id}")
    public ResponseEntity<Void> eliminarEntrenamiento(
            @PathVariable String id,
            @RequestBody(required = false) RegistroEntrenamientoDeleteRequestDto request) {
        entrenamientoService.eliminarEntrenamiento(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/entrenamientos")
    public ResponseEntity<List<RegistroEntrenamientoResponseDto>> obtenerHistorico() {
        return ResponseEntity.ok(entrenamientoService.obtenerHistorico());
    }

    @GetMapping("/entrenamientos/{id}")
    public ResponseEntity<RegistroEntrenamientoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(entrenamientoService.obtenerPorId(id));
    }
}
