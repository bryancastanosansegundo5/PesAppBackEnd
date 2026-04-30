package com.pesapp.pesapp.entrenamientos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.entrenamientos.model.dto.RegistroEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EntrenamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntrenamientoService entrenamientoService;

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeActualizarEntrenamientoHistorico() throws Exception {
        RegistroEntrenamientoResponseDto response = new RegistroEntrenamientoResponseDto();
        response.setId("123");
        response.setClientId("entrenamiento-cliente-1");
        response.setNombreSesion("Empuje");
        response.setFechaInicio(LocalDateTime.of(2026, 4, 30, 10, 0));
        response.setFechaFin(LocalDateTime.of(2026, 4, 30, 11, 0));
        response.setVersion(4L);
        response.setEjercicios(List.of());

        when(entrenamientoService.actualizarEntrenamiento(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/entrenamientos/123")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": "123",
                                  "clientId": "entrenamiento-cliente-1",
                                  "nombreSesion": "Empuje",
                                  "version": 3,
                                  "ejercicios": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.clientId").value("entrenamiento-cliente-1"))
                .andExpect(jsonPath("$.version").value(4));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeEliminarEntrenamientoHistorico() throws Exception {
        doNothing().when(entrenamientoService).eliminarEntrenamiento(any(), any());

        mockMvc.perform(delete("/api/entrenamientos/123")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteBody("123", "entrenamiento-cliente-1", 4L))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeEliminarEntrenamientoHistoricoUsandoClientIdEnLaRuta() throws Exception {
        doNothing().when(entrenamientoService).eliminarEntrenamiento(any(), any());

        mockMvc.perform(delete("/api/entrenamientos/entrenamiento-cliente-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteBody(null, "entrenamiento-cliente-1", 4L))))
                .andExpect(status().isNoContent());
    }

    private record DeleteBody(String id, String clientId, Long version) {}
}
