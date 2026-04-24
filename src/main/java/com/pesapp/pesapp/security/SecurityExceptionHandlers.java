package com.pesapp.pesapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesapp.pesapp.entrenamientos.model.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityExceptionHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        escribirRespuesta(
                response,
                HttpStatus.UNAUTHORIZED,
                "No autorizado",
                List.of("Debes iniciar sesion para acceder a este recurso"));
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        escribirRespuesta(
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                List.of("No tienes permisos para acceder a este recurso"));
    }

    private void escribirRespuesta(
            HttpServletResponse response,
            HttpStatus status,
            String error,
            List<String> mensajes) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponseDto(LocalDateTime.now(), status.value(), error, mensajes));
    }
}
