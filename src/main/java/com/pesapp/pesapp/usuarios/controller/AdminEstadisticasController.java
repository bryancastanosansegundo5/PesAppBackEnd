package com.pesapp.pesapp.usuarios.controller;

import com.pesapp.pesapp.usuarios.model.dto.EstadisticasInicioSesionResponseDto;
import com.pesapp.pesapp.usuarios.service.RegistroAccesoAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/estadisticas")
public class AdminEstadisticasController {

    private final RegistroAccesoAppService registroAccesoAppService;

    @GetMapping("/inicios-sesion")
    public ResponseEntity<EstadisticasInicioSesionResponseDto> obtenerIniciosSesion(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(registroAccesoAppService.obtenerEstadisticasInicioSesion(dias));
    }
}
