package com.pesapp.pesapp.usuarios.service;

import com.pesapp.pesapp.usuarios.model.dto.EstadisticasInicioSesionResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.TipoAccesoApp;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;

public interface RegistroAccesoAppService {

    void registrarAcceso(UsuarioVO usuario, TipoAccesoApp tipo);

    EstadisticasInicioSesionResponseDto obtenerEstadisticasInicioSesion(int dias);
}
