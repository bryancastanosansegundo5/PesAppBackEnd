package com.pesapp.pesapp.initializer;

import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(1)
@ConditionalOnProperty(name = "app.initializer.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initializer.default-user-name}")
    private String defaultUserName;

    @Value("${app.initializer.default-user-email}")
    private String defaultUserEmail;

    @Value("${app.initializer.default-user-username:}")
    private String defaultUserUsername;

    @Value("${app.initializer.default-user-password}")
    private String defaultUserPassword;

    @Value("${app.initializer.default-user-role}")
    private String defaultUserRole;

    @Override
    @Transactional
    public void run(String... args) {
        String email = defaultUserEmail.trim().toLowerCase();
        String username = resolverUsername(email);
        RolUsuario rol = parseRol(defaultUserRole);

        UsuarioVO existente = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existente != null) {
            if (existente.getUsername() == null || existente.getUsername().isBlank()) {
                existente.setUsername(username);
            }
            if (existente.getRol() != rol) {
                existente.setRol(rol);
            }
            if (!existente.isActivo()) {
                existente.setActivo(true);
            }
            return;
        }

        UsuarioVO usuario = new UsuarioVO();
        usuario.setNombre(defaultUserName.trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(defaultUserPassword));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
    }

    private RolUsuario parseRol(String rol) {
        try {
            return RolUsuario.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("DEFAULT_USER_ROLE debe ser ADMIN, COACH o USUARIO");
        }
    }

    private String resolverUsername(String email) {
        if (defaultUserUsername != null && !defaultUserUsername.isBlank()) {
            return defaultUserUsername.trim().toLowerCase();
        }

        String localPart = email.substring(0, email.indexOf('@')).toLowerCase();
        return localPart.isBlank() ? "admin" : localPart;
    }
}
