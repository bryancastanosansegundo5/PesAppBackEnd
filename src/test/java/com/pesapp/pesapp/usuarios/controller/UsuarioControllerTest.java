package com.pesapp.pesapp.usuarios.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.usuarios.exception.ConflictException;
import com.pesapp.pesapp.usuarios.model.dto.ActualizarPerfilUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.DisponibilidadUsernameResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeObtenerPerfilDelUsuarioAutenticadoPorHttp() throws Exception {
        when(usuarioService.obtenerPerfil()).thenReturn(crearUsuarioResponse("bryan", "bryan@pesapp.local"));

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Bryan"))
                .andExpect(jsonPath("$.username").value("bryan"))
                .andExpect(jsonPath("$.email").value("bryan@pesapp.local"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeActualizarPerfilDelUsuarioAutenticadoPorHttp() throws Exception {
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan Garcia");
        request.setUsername("bryan");
        request.setEmail("");

        UsuarioResponseDto response = crearUsuarioResponse("bryan", null);
        response.setNombre("Bryan Garcia");
        when(usuarioService.actualizarPerfil(org.mockito.ArgumentMatchers.any())).thenReturn(response);

                mockMvc.perform(patch("/api/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Bryan Garcia"))
                .andExpect(jsonPath("$.username").value("bryan"))
                .andExpect(jsonPath("$.email").value(nullValue()));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeResponder409SiElUsernameYaExiste() throws Exception {
        ActualizarPerfilUsuarioRequestDto request = new ActualizarPerfilUsuarioRequestDto();
        request.setNombre("Bryan");
        request.setUsername("duplicado");
        request.setEmail("bryan@pesapp.local");

        when(usuarioService.actualizarPerfil(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ConflictException("Ya existe un usuario registrado con ese nombre de usuario"));

        mockMvc.perform(patch("/api/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensajes[0]").value("Ya existe un usuario registrado con ese nombre de usuario"));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeDevolverDisponibilidadTrue() throws Exception {
        when(usuarioService.comprobarDisponibilidadUsername("libre"))
                .thenReturn(new DisponibilidadUsernameResponseDto(true));

        mockMvc.perform(get("/api/usuarios/disponibilidad-username").param("username", "libre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    @WithMockUser(username = "bryan", roles = "USUARIO")
    void debeDevolverDisponibilidadFalse() throws Exception {
        when(usuarioService.comprobarDisponibilidadUsername("ocupado"))
                .thenReturn(new DisponibilidadUsernameResponseDto(false));

        mockMvc.perform(get("/api/usuarios/disponibilidad-username").param("username", "ocupado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    private UsuarioResponseDto crearUsuarioResponse(String username, String email) {
        UsuarioResponseDto response = new UsuarioResponseDto();
        response.setId(1L);
        response.setNombre("Bryan");
        response.setUsername(username);
        response.setEmail(email);
        response.setRol(RolUsuario.USUARIO);
        response.setActivo(true);
        response.setCreatedAt(LocalDateTime.of(2026, 4, 20, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 12, 30));
        return response;
    }
}
