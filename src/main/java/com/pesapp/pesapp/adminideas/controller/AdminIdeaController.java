package com.pesapp.pesapp.adminideas.controller;

import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaCreateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaEstadoUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaResponseDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaUpdateRequestDto;
import com.pesapp.pesapp.adminideas.service.AdminIdeaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ideas")
public class AdminIdeaController {

    private final AdminIdeaService adminIdeaService;

    @GetMapping
    public ResponseEntity<List<AdminIdeaResponseDto>> obtenerIdeas() {
        return ResponseEntity.ok(adminIdeaService.obtenerIdeas());
    }

    @PostMapping
    public ResponseEntity<AdminIdeaResponseDto> guardarIdea(@Valid @RequestBody AdminIdeaCreateRequestDto request) {
        return ResponseEntity.ok(adminIdeaService.guardarIdea(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminIdeaResponseDto> actualizarIdea(
            @PathVariable Long id,
            @Valid @RequestBody AdminIdeaUpdateRequestDto request) {
        return ResponseEntity.ok(adminIdeaService.actualizarIdea(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<AdminIdeaResponseDto> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody AdminIdeaEstadoUpdateRequestDto request) {
        return ResponseEntity.ok(adminIdeaService.actualizarEstado(id, request));
    }
}
