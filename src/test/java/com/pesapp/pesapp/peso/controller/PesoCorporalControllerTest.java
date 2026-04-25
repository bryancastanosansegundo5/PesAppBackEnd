package com.pesapp.pesapp.peso.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.service.PesoCorporalService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class PesoCorporalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PesoCorporalService pesoCorporalService;

    @Test
    void debeResponder401SiNoHayAutenticacion() throws Exception {
        mockMvc.perform(get("/api/peso")).andExpect(status().isUnauthorized());
        verifyNoInteractions(pesoCorporalService);
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeDevolverHistoricoNormalizado() throws Exception {
        when(pesoCorporalService.obtenerHistorico()).thenReturn(List.of(crearRespuesta()));

        mockMvc.perform(get("/api/peso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("12"))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].peso").value(82.4))
                .andExpect(jsonPath("$[0].fechaRegistro").value("2026-04-25"))
                .andExpect(jsonPath("$[0].clientId").value("peso-2026-04-25"))
                .andExpect(jsonPath("$[0].version").value(3));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeGuardarPesoDeHoy() throws Exception {
        when(pesoCorporalService.guardarPesoHoy(org.mockito.ArgumentMatchers.any())).thenReturn(crearRespuesta());

        mockMvc.perform(put("/api/peso/hoy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "peso", new BigDecimal("82.40"),
                                "clientId", "peso-2026-04-25",
                                "version", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("12"))
                .andExpect(jsonPath("$.peso").value(82.4))
                .andExpect(jsonPath("$.updatedAt").value("2026-04-25T12:15:00"));
    }

    private PesoCorporalResponseDto crearRespuesta() {
        PesoCorporalResponseDto dto = new PesoCorporalResponseDto();
        dto.setId("12");
        dto.setUserId(1L);
        dto.setClientId("peso-2026-04-25");
        dto.setPeso(new BigDecimal("82.40"));
        dto.setFechaRegistro(LocalDate.of(2026, 4, 25));
        dto.setCreatedAt(LocalDateTime.of(2026, 4, 25, 8, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 12, 15));
        dto.setVersion(3L);
        return dto;
    }
}
