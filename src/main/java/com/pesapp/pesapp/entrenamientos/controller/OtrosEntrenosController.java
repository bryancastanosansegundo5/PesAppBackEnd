package com.pesapp.pesapp.entrenamientos.controller;

import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoResumenDto;
import com.pesapp.pesapp.entrenamientos.service.OtrosEntrenosService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OtrosEntrenosController {

    private final OtrosEntrenosService otrosEntrenosService;

    @GetMapping("/otros-entrenos")
    public ResponseEntity<List<OtroEntrenoResumenDto>> obtenerHistoricoAgrupado() {
        return ResponseEntity.ok(otrosEntrenosService.obtenerHistoricoAgrupado());
    }
}
