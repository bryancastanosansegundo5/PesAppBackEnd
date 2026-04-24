package com.pesapp.pesapp.initializer;

import java.util.Map;
import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class UsuarioRolColumnInitializer implements CommandLineRunner {

    private static final int REQUIRED_ROL_LENGTH = 30;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (!isMySqlDatabase()) {
            return;
        }

        Map<String, Object> columnInfo = obtenerInfoColumnaRol();
        if (columnInfo == null || !requiereNormalizacion(columnInfo)) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE usuarios MODIFY COLUMN rol VARCHAR(30) NOT NULL");
        log.info("Columna usuarios.rol normalizada a VARCHAR(30) para soportar los roles actuales.");
    }

    private boolean isMySqlDatabase() {
        try (var connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("mysql");
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo verificar el motor de base de datos", exception);
        }
    }

    private Map<String, Object> obtenerInfoColumnaRol() {
        var resultados = jdbcTemplate.queryForList("""
                SELECT DATA_TYPE, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'usuarios'
                  AND COLUMN_NAME = 'rol'
                """);

        if (resultados.isEmpty()) {
            return null;
        }

        return resultados.getFirst();
    }

    private boolean requiereNormalizacion(Map<String, Object> columnInfo) {
        String dataType = toLowerCase(columnInfo.get("DATA_TYPE"));
        String columnType = toLowerCase(columnInfo.get("COLUMN_TYPE"));
        Integer maxLength = toInteger(columnInfo.get("CHARACTER_MAXIMUM_LENGTH"));

        if ("enum".equals(dataType) || columnType.startsWith("enum(")) {
            return true;
        }

        return maxLength != null && maxLength < REQUIRED_ROL_LENGTH;
    }

    private String toLowerCase(Object value) {
        return value == null ? "" : value.toString().toLowerCase();
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
