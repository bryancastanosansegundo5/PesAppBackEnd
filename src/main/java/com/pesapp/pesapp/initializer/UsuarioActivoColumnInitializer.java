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
public class UsuarioActivoColumnInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (!isMySqlDatabase()) {
            return;
        }

        if (obtenerInfoColumnaActivo() == null) {
            jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE");
            log.info("Columna usuarios.activo creada.");
            return;
        }

        jdbcTemplate.execute("UPDATE usuarios SET activo = TRUE WHERE activo IS NULL");
        jdbcTemplate.execute("ALTER TABLE usuarios MODIFY COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE");
        log.info("Columna usuarios.activo normalizada a BOOLEAN NOT NULL DEFAULT TRUE.");
    }

    private boolean isMySqlDatabase() {
        try (var connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            String productNameNormalizado = productName == null ? "" : productName.toLowerCase();
            return productNameNormalizado.contains("mysql") || productNameNormalizado.contains("mariadb");
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo verificar el motor de base de datos", exception);
        }
    }

    private Map<String, Object> obtenerInfoColumnaActivo() {
        var resultados = jdbcTemplate.queryForList("""
                SELECT DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'usuarios'
                  AND COLUMN_NAME = 'activo'
                """);

        if (resultados.isEmpty()) {
            return null;
        }

        return resultados.getFirst();
    }
}
