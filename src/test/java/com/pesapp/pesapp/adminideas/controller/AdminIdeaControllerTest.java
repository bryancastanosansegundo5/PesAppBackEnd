package com.pesapp.pesapp.adminideas.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaResponseDto;
import com.pesapp.pesapp.adminideas.service.AdminIdeaService;
import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AdminIdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AdminIdeaService adminIdeaService;

    @Test
    void debeResponder401SiNoHayAutenticacion() throws Exception {
        mockMvc.perform(get("/api/admin/ideas")).andExpect(status().isUnauthorized());
        verifyNoInteractions(adminIdeaService);
    }

    @Test
    @WithMockUser(username = "coach", roles = "COACH")
    void debeResponder403SiElRolNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/ideas")).andExpect(status().isForbidden());
        verifyNoInteractions(adminIdeaService);
    }

    @Test
    void debeAceptarCookieJwtDeAdminReal() throws Exception {
        Cookie accessCookie = loginYObtenerAccessCookie();
        when(adminIdeaService.obtenerIdeas()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/ideas")
                        .cookie(accessCookie)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    @Test
    void debeProbarTodasLasCookiesAccessTokenSiHayDuplicadas() throws Exception {
        Cookie accessCookie = loginYObtenerAccessCookie();
        Cookie cookieAntigua = new Cookie("pesapp_access_token", "token-antiguo-invalido");
        when(adminIdeaService.obtenerIdeas()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/ideas")
                        .cookie(cookieAntigua, accessCookie)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    private Cookie loginYObtenerAccessCookie() throws Exception {
        UsuarioVO admin = new UsuarioVO();
        admin.setNombre("Admin Test");
        admin.setUsername("adminideas-" + java.util.UUID.randomUUID());
        admin.setEmail(admin.getUsername() + "@pesapp.local");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setActivo(true);
        usuarioRepository.saveAndFlush(admin);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "username", admin.getUsername(),
                                "password", "ChangeMe123!"))))
                .andExpect(status().isOk())
                .andReturn();

        return login.getResponse().getCookie("pesapp_access_token");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void debeDevolverIdeasNormalizadas() throws Exception {
        when(adminIdeaService.obtenerIdeas()).thenReturn(List.of(
                crearRespuesta("2", "idea-local-2", "Mas reciente", true, "2026-04-29T12:00:00", 3L),
                crearRespuesta("1", "idea-local-1", "Anterior", false, "2026-04-29T09:00:00", 1L)));

        mockMvc.perform(get("/api/admin/ideas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].clientId").value("idea-local-2"))
                .andExpect(jsonPath("$[0].titulo").value("Mas reciente"))
                .andExpect(jsonPath("$[0].descripcion").value("Descripcion Mas reciente"))
                .andExpect(jsonPath("$[0].completada").value(true))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-04-29T12:00:00"))
                .andExpect(jsonPath("$[0].version").value(3))
                .andExpect(jsonPath("$[1].id").value("1"))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void debeGuardarIdeaConPostIdempotente() throws Exception {
        when(adminIdeaService.guardarIdea(org.mockito.ArgumentMatchers.any()))
                .thenReturn(crearRespuesta("7", "idea-local-7", "Idea consolidada", true, "2026-04-29T12:10:00", 4L));

        mockMvc.perform(post("/api/admin/ideas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "clientId", "idea-local-7",
                                "titulo", "Idea consolidada",
                                "descripcion", "Descripcion Idea consolidada",
                                "completada", true,
                                "activo", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.clientId").value("idea-local-7"))
                .andExpect(jsonPath("$.titulo").value("Idea consolidada"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.version").value(4));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void debeActualizarIdeaConVersionCorrecta() throws Exception {
        when(adminIdeaService.actualizarIdea(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(crearRespuesta("7", "idea-local-7", "Idea cerrada", true, "2026-04-29T12:20:00", 5L));

        mockMvc.perform(patch("/api/admin/ideas/7")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "titulo", "Idea cerrada",
                                "descripcion", "Descripcion Idea cerrada",
                                "completada", true,
                                "activo", true,
                                "version", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Idea cerrada"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.updatedAt").value("2026-04-29T12:20:00"))
                .andExpect(jsonPath("$.version").value(5));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void debeResponder409SiHayConflictoDeVersion() throws Exception {
        when(adminIdeaService.actualizarIdea(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new OptimisticLockException("La version enviada no coincide con la version actual del recurso"));

        mockMvc.perform(patch("/api/admin/ideas/7")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "titulo", "Idea cerrada",
                                "descripcion", "Descripcion Idea cerrada",
                                "completada", true,
                                "version", 4))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de version"))
                .andExpect(jsonPath("$.mensaje").value("La version enviada no coincide con la version actual del recurso"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void debeActualizarEstadoConRespuestaNormalizada() throws Exception {
        when(adminIdeaService.actualizarEstado(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(crearRespuesta("7", "idea-local-7", "Idea cerrada", false, "2026-04-29T12:21:00", 6L));

        mockMvc.perform(patch("/api/admin/ideas/7/estado")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "activo", false,
                                "version", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.version").value(6));
    }

    private AdminIdeaResponseDto crearRespuesta(
            String id,
            String clientId,
            String titulo,
            boolean activo,
            String updatedAt,
            Long version) {
        AdminIdeaResponseDto dto = new AdminIdeaResponseDto();
        dto.setId(id);
        dto.setClientId(clientId);
        dto.setTitulo(titulo);
        dto.setDescripcion("Descripcion " + titulo);
        dto.setCompletada(true);
        dto.setActivo(activo);
        dto.setCreatedAt(LocalDateTime.of(2026, 4, 29, 8, 0));
        dto.setUpdatedAt(LocalDateTime.parse(updatedAt));
        dto.setVersion(version);
        return dto;
    }
}
