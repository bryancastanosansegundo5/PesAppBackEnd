package com.pesapp.pesapp.usuarios.service.impl;

import com.pesapp.pesapp.usuarios.model.dto.EstadisticaInicioSesionDiaDto;
import com.pesapp.pesapp.usuarios.model.dto.EstadisticasInicioSesionResponseDto;
import com.pesapp.pesapp.usuarios.model.vo.RegistroAccesoAppVO;
import com.pesapp.pesapp.usuarios.model.vo.TipoAccesoApp;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.repository.RegistroAccesoAppRepository;
import com.pesapp.pesapp.usuarios.service.RegistroAccesoAppService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroAccesoAppServiceImpl implements RegistroAccesoAppService {

    private static final int DIAS_MAXIMOS = 365;
    private static final int DIAS_POR_DEFECTO = 30;

    private final RegistroAccesoAppRepository registroAccesoAppRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAcceso(UsuarioVO usuario, TipoAccesoApp tipo) {
        RegistroAccesoAppVO registro = new RegistroAccesoAppVO();
        registro.setUsuario(usuario);
        registro.setTipo(tipo);
        registro.setFechaAcceso(LocalDateTime.now());
        registroAccesoAppRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasInicioSesionResponseDto obtenerEstadisticasInicioSesion(int dias) {
        int diasNormalizados = normalizarDias(dias);
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(diasNormalizados - 1L);
        LocalDateTime desdeInicio = desde.atStartOfDay();
        LocalDateTime hastaFin = hoy.plusDays(1).atStartOfDay();

        var registros = registroAccesoAppRepository.findAllByFechaAccesoBetween(desdeInicio, hastaFin);
        Map<LocalDate, Long> conteoPorDia = registros.stream()
                .collect(Collectors.groupingBy(
                        registro -> registro.getFechaAcceso().toLocalDate(),
                        Collectors.counting()));

        var diasSerie = LongStream.range(0, diasNormalizados)
                .mapToObj(desde::plusDays)
                .map(fecha -> new EstadisticaInicioSesionDiaDto(fecha, conteoPorDia.getOrDefault(fecha, 0L)))
                .toList();

        long usuariosUnicos = registros.stream()
                .map(RegistroAccesoAppVO::getUsuario)
                .filter(usuario -> usuario != null && usuario.getId() != null)
                .map(UsuarioVO::getId)
                .collect(Collectors.toSet())
                .size();
        long total = diasSerie.stream()
                .mapToLong(EstadisticaInicioSesionDiaDto::iniciosSesion)
                .sum();

        return new EstadisticasInicioSesionResponseDto(diasSerie, total, usuariosUnicos);
    }

    private int normalizarDias(int dias) {
        if (dias <= 0) {
            return DIAS_POR_DEFECTO;
        }
        return Math.min(dias, DIAS_MAXIMOS);
    }
}
