package com.pesapp.pesapp.usuarios.service;

import com.pesapp.pesapp.usuarios.model.dto.AuthResponseDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarEstadoUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CambiarRolUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.CrearUsuarioAdminRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.LoginRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.RegistroUsuarioRequestDto;
import com.pesapp.pesapp.usuarios.model.dto.UsuarioResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import java.util.List;

public interface UsuarioService {

    AuthResponseDto registrar(RegistroUsuarioRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    UsuarioVO obtenerUsuarioAutenticado();

    UsuarioResponseDto obtenerPerfil();

    List<UsuarioResponseDto> obtenerUsuarios();

    UsuarioResponseDto crearUsuarioDesdeAdmin(CrearUsuarioAdminRequestDto request);

    UsuarioResponseDto cambiarRol(Long usuarioId, CambiarRolUsuarioRequestDto request);

    UsuarioResponseDto cambiarEstado(Long usuarioId, CambiarEstadoUsuarioRequestDto request);
}
