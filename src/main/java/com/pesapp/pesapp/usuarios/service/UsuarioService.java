package com.pesapp.pesapp.usuarios.service;

import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.ActualizarPerfilUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.AuthSessionDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarEstadoUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarRolUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.DisponibilidadUsernameResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.LogoutResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import java.util.List;

public interface UsuarioService {

    AuthSessionDto registrar(RegistroUsuarioRequestDto request);

    AuthSessionDto login(LoginRequestDto request);

    AuthSessionDto refrescarSesion(String refreshToken);

    LogoutResponseDto logout(String refreshToken);

    UsuarioVO obtenerUsuarioAutenticado();

    UsuarioResponseDto obtenerPerfil();

    UsuarioResponseDto actualizarPerfil(ActualizarPerfilUsuarioRequestDto request);

    DisponibilidadUsernameResponseDto comprobarDisponibilidadUsername(String username);

    List<UsuarioResponseDto> obtenerUsuarios();

    UsuarioResponseDto crearUsuarioDesdeAdmin(CrearUsuarioAdminRequestDto request);

    UsuarioResponseDto cambiarRol(Long usuarioId, CambiarRolUsuarioRequestDto request);

    UsuarioResponseDto cambiarEstado(Long usuarioId, CambiarEstadoUsuarioRequestDto request);
}
