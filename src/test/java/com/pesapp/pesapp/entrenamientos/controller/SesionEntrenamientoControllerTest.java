package com.pesapp.pesapp.entrenamientos.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pesapp.pesapp.entrenamientos.model.dto.PlantillaSesionEntrenamientoResponseDto;
import com.pesapp.pesapp.entrenamientos.service.EntrenamientoService;
import com.pesapp.pesapp.entrenamientos.service.SesionEntrenamientoService;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
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
class SesionEntrenamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SesionEntrenamientoService sesionEntrenamientoService;

    @MockBean
    private EntrenamientoService entrenamientoService;

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeExponerIdPersistidoEnListado() throws Exception {
        PlantillaSesionEntrenamientoResponseDto response = new PlantillaSesionEntrenamientoResponseDto();
        response.setId("42");
        response.setIdSesion("42");
        response.setClientId("sesion-local-42");
        response.setNombreSesion("Push");
        when(sesionEntrenamientoService.obtenerTodas()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/sesiones-entrenamiento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("42"))
                .andExpect(jsonPath("$[0].idSesion").value("42"))
                .andExpect(jsonPath("$[0].clientId").value("sesion-local-42"));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeResponder409ConMensajeFuncionalSiNoSePuedeEliminar() throws Exception {
        doThrow(new ConflictException("No se puede eliminar la sesion porque tiene entrenamientos asociados"))
                .when(sesionEntrenamientoService)
                .eliminar(42L);

        mockMvc.perform(delete("/api/sesiones-entrenamiento/42"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.error").value("Conflicto"))
                .andExpect(jsonPath("$.mensaje")
                        .value("No se puede eliminar la sesion porque tiene entrenamientos asociados"));
    }
}
