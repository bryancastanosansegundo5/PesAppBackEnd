package com.pesapp.pesapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.usuarios.model.vo.RolUsuario;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void debeCargarUsuarioPorEmailOUsername() {
        UsuarioVO usuario = crearUsuario(true);
        when(usuarioRepository.findByUsernameIgnoreCase("bryan"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("bryan");

        assertThat(userDetails.getUsername()).isEqualTo("bryan");
        assertThat(userDetails.getPassword()).isEqualTo("hash");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void debeFallarSiUsuarioEstaDesactivado() {
        UsuarioVO usuario = crearUsuario(false);
        when(usuarioRepository.findByUsernameIgnoreCase("bryan"))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("bryan"))
                .isInstanceOf(DisabledException.class)
                .hasMessage("El usuario esta desactivado");
    }

    private UsuarioVO crearUsuario(boolean activo) {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(1L);
        usuario.setNombre("Bryan");
        usuario.setUsername("bryan");
        usuario.setEmail("bryan@pesapp.local");
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setActivo(activo);
        return usuario;
    }
}
