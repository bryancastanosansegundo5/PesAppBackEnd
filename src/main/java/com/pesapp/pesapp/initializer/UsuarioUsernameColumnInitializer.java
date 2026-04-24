package com.pesapp.pesapp.initializer;

import java.util.List;
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
public class UsuarioUsernameColumnInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (!isMySqlDatabase()) {
            return;
        }

        asegurarColumnaUsername();
        rellenarUsernameEnBlanco();
        asegurarIndiceUnico();
    }

    private boolean isMySqlDatabase() {
        try (var connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("mysql");
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo verificar el motor de base de datos", exception);
        }
    }

    private void asegurarColumnaUsername() {
        List<Map<String, Object>> resultados = jdbcTemplate.queryForList("""
                SELECT COLUMN_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'usuarios'
                  AND COLUMN_NAME = 'username'
                """);

        if (!resultados.isEmpty()) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN username VARCHAR(60) NULL AFTER nombre");
        log.info("Columna usuarios.username creada.");
    }

    private void rellenarUsernameEnBlanco() {
        jdbcTemplate.execute("""
                UPDATE usuarios
                   SET username = LOWER(
                       CASE
                           WHEN email IS NOT NULL AND LOCATE('@', email) > 1
                               THEN CONCAT(
                                   REGEXP_REPLACE(SUBSTRING_INDEX(email, '@', 1), '[^A-Za-z0-9._-]', ''),
                                   '_',
                                   id
                               )
                           ELSE CONCAT('user_', id)
                       END
                   )
                 WHERE username IS NULL
                    OR TRIM(username) = ''
                """);

        jdbcTemplate.execute("ALTER TABLE usuarios MODIFY COLUMN username VARCHAR(60) NOT NULL");
    }

    private void asegurarIndiceUnico() {
        List<Map<String, Object>> resultados = jdbcTemplate.queryForList("""
                SELECT INDEX_NAME
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'usuarios'
                  AND INDEX_NAME IN ('uq_usuarios_username', 'uk_usuarios_username')
                """);

        if (!resultados.isEmpty()) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE usuarios ADD CONSTRAINT uq_usuarios_username UNIQUE (username)");
        log.info("Restriccion unica uq_usuarios_username creada.");
    }
}
