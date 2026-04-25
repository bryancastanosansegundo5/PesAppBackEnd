package com.pesapp.pesapp.peso.controller;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import com.pesapp.pesapp.peso.service.PesoCorporalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/peso")
public class PesoCorporalController {

    private final PesoCorporalService pesoCorporalService;

    @GetMapping
    public ResponseEntity<List<PesoCorporalResponseDto>> obtenerHistorico() {
        return ResponseEntity.ok(pesoCorporalService.obtenerHistorico());
    }

    @PutMapping("/hoy")
    public ResponseEntity<PesoCorporalResponseDto> guardarPesoHoy(@Valid @RequestBody PesoHoyRequestDto request) {
        return ResponseEntity.ok(pesoCorporalService.guardarPesoHoy(request));
    }

    @PostMapping
    public ResponseEntity<PesoCorporalResponseDto> guardar(@Valid @RequestBody PesoCorporalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pesoCorporalService.guardar(request));
    }
}
