package com.pesapp.pesapp.entrenamientos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoChartPointDto;
import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoEntradaDto;
import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoResumenDto;
import com.pesapp.pesapp.entrenamientos.service.OtrosEntrenosService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OtrosEntrenosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtrosEntrenosService otrosEntrenosService;

    @Test
    void debeResponder401SiNoHayAutenticacion() throws Exception {
        mockMvc.perform(get("/api/otros-entrenos")).andExpect(status().isUnauthorized());
        verifyNoInteractions(otrosEntrenosService);
    }

    @Test
    @WithMockUser(username = "user@pesapp.local", roles = "USUARIO")
    void debeResponder200ConListaVacia() throws Exception {
        when(otrosEntrenosService.obtenerHistoricoAgrupado()).thenReturn(List.of());

        mockMvc.perform(get("/api/otros-entrenos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "user@pesapp.local", roles = "USUARIO")
    void debeExponerContratoEsperado() throws Exception {
        when(otrosEntrenosService.obtenerHistoricoAgrupado()).thenReturn(List.of(crearRespuesta()));

        mockMvc.perform(get("/api/otros-entrenos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("catalogo:44"))
                .andExpect(jsonPath("$[0].nombre").value("Press banca"))
                .andExpect(jsonPath("$[0].sesionesTotales").value(2))
                .andExpect(jsonPath("$[0].pesoMaximoHistorico").value(85.00))
                .andExpect(jsonPath("$[0].volumenHistorico").value(1905.00))
                .andExpect(jsonPath("$[0].chartData", hasSize(1)))
                .andExpect(jsonPath("$[0].entradas", hasSize(1)))
                .andExpect(jsonPath("$[0].entradas[0].seriesRealizadas").value(2))
                .andExpect(jsonPath("$[0].entradas[0].repeticionesTotales").value(14));
    }

    private OtroEntrenoResumenDto crearRespuesta() {
        OtroEntrenoChartPointDto chartPoint = new OtroEntrenoChartPointDto();
        chartPoint.setFecha(LocalDateTime.of(2026, 4, 20, 19, 0));
        chartPoint.setValor(new BigDecimal("85.00"));

        OtroEntrenoEntradaDto entrada = new OtroEntrenoEntradaDto();
        entrada.setId("press-banca-1");
        entrada.setFecha(LocalDateTime.of(2026, 4, 20, 19, 0));
        entrada.setNombreSesion("Push");
        entrada.setSeriesPlanificadas(3);
        entrada.setRepeticionesPlanificadas(8);
        entrada.setPesoPlanificado(new BigDecimal("82.50"));
        entrada.setAlturaBanco("Plano");
        entrada.setAgarre("Pronado");
        entrada.setSeriesRealizadas(2);
        entrada.setRepeticionesTotales(14);
        entrada.setVolumenTotal(new BigDecimal("1150.00"));
        entrada.setPesoMaximo(new BigDecimal("85.00"));

        OtroEntrenoResumenDto dto = new OtroEntrenoResumenDto();
        dto.setId("catalogo:44");
        dto.setNombre("Press banca");
        dto.setDescripcion("Barra olimpica");
        dto.setGrupoMuscular("Pecho");
        dto.setPatronMovimiento("Empuje horizontal");
        dto.setEquipamiento("Barra");
        dto.setAgarre("Pronado");
        dto.setAlturaBanco("Plano");
        dto.setSesionesTotales(2);
        dto.setUltimoRegistro(LocalDateTime.of(2026, 4, 20, 19, 0));
        dto.setPesoMaximoHistorico(new BigDecimal("85.00"));
        dto.setVolumenHistorico(new BigDecimal("1905.00"));
        dto.setChartData(List.of(chartPoint));
        dto.setEntradas(List.of(entrada));
        return dto;
    }
}
