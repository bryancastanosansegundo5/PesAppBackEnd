package com.pesapp.pesapp.usuarios.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LoginRequestDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void debeAceptarUsernameOIdentificadorComoAlias() throws Exception {
        LoginRequestDto desdeUsername = objectMapper.readValue(
                """
                {"username":"bryan","password":"Secret123"}
                """,
                LoginRequestDto.class);
        LoginRequestDto desdeIdentificador = objectMapper.readValue(
                """
                {"identificador":"bryan","password":"Secret123"}
                """,
                LoginRequestDto.class);

        assertThat(desdeUsername.getUsername()).isEqualTo("bryan");
        assertThat(desdeIdentificador.getUsername()).isEqualTo("bryan");
    }
}
