package com.pesapp.pesapp.entrenamientos.service.impl;

import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoChartPointDto;
import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoEntradaDto;
import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoResumenDto;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroSerieVO;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.entrenamientos.service.OtrosEntrenosService;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtrosEntrenosServiceImpl implements OtrosEntrenosService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final RegistroEjercicioRepository registroEjercicioRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<OtroEntrenoResumenDto> obtenerHistoricoAgrupado() {
        UsuarioVO usuario = usuarioService.obtenerUsuarioAutenticado();
        List<RegistroEjercicioVO> ejercicios = registroEjercicioRepository.findHistoricoVisibleByUsuarioId(usuario.getId());
        if (ejercicios.isEmpty()) {
            return List.of();
        }

        Map<String, OtroEntrenoResumenDto> agrupados = new LinkedHashMap<>();
        for (RegistroEjercicioVO ejercicio : ejercicios) {
            String clave = resolverClaveAgrupacion(ejercicio);
            OtroEntrenoResumenDto resumen = agrupados.computeIfAbsent(clave, ignored -> crearResumen(ejercicio, clave));
            OtroEntrenoEntradaDto entrada = crearEntrada(ejercicio);
            resumen.getEntradas().add(entrada);
            actualizarResumen(resumen, ejercicio, entrada);
        }

        return agrupados.values().stream()
                .peek(this::ordenarColecciones)
                .sorted(Comparator.comparing(OtroEntrenoResumenDto::getUltimoRegistro, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OtroEntrenoResumenDto::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private OtroEntrenoResumenDto crearResumen(RegistroEjercicioVO ejercicio, String clave) {
        OtroEntrenoResumenDto resumen = new OtroEntrenoResumenDto();
        resumen.setId(clave);
        resumen.setNombre(ejercicio.getNombre());
        resumen.setDescripcion(ejercicio.getDescripcion());
        resumen.setGrupoMuscular(ejercicio.getGrupoMuscular());
        resumen.setPatronMovimiento(ejercicio.getPatronMovimiento());
        resumen.setEquipamiento(ejercicio.getEquipamiento());
        resumen.setAgarre(ejercicio.getAgarre());
        resumen.setAlturaBanco(ejercicio.getAlturaBanco());
        resumen.setSesionesTotales(0);
        resumen.setPesoMaximoHistorico(ZERO);
        resumen.setVolumenHistorico(ZERO);
        return resumen;
    }

    private OtroEntrenoEntradaDto crearEntrada(RegistroEjercicioVO ejercicio) {
        OtroEntrenoEntradaDto entrada = new OtroEntrenoEntradaDto();
        entrada.setId(resolverIdEntrada(ejercicio));
        entrada.setFecha(ejercicio.getRegistroEntrenamiento().getFechaFinalizacion());
        entrada.setNombreSesion(ejercicio.getRegistroEntrenamiento().getNombreSesion());
        entrada.setSeriesPlanificadas(ejercicio.getSeriesBase());
        entrada.setRepeticionesPlanificadas(ejercicio.getRepeticionesBase());
        entrada.setPesoPlanificado(scale(ejercicio.getPesoBase()));
        entrada.setAlturaBanco(ejercicio.getAlturaBanco());
        entrada.setAgarre(ejercicio.getAgarre());

        List<RegistroSerieVO> seriesOrdenadas = ejercicio.getSeriesRealizadas().stream()
                .sorted(Comparator.comparing(RegistroSerieVO::getOrden).thenComparing(RegistroSerieVO::getId))
                .toList();

        entrada.setSeriesRealizadas(seriesOrdenadas.size());
        entrada.setRepeticionesTotales(seriesOrdenadas.stream()
                .map(RegistroSerieVO::getRepeticiones)
                .filter(repeticiones -> repeticiones != null)
                .reduce(0, Integer::sum));
        entrada.setVolumenTotal(seriesOrdenadas.stream()
                .map(this::calcularVolumenSerie)
                .reduce(ZERO, BigDecimal::add));
        entrada.setPesoMaximo(seriesOrdenadas.stream()
                .map(RegistroSerieVO::getPeso)
                .filter(peso -> peso != null)
                .map(this::scale)
                .max(BigDecimal::compareTo)
                .orElse(ZERO));
        return entrada;
    }

    private void actualizarResumen(OtroEntrenoResumenDto resumen, RegistroEjercicioVO ejercicio, OtroEntrenoEntradaDto entrada) {
        resumen.setSesionesTotales(resumen.getSesionesTotales() + 1);
        LocalDateTime fecha = ejercicio.getRegistroEntrenamiento().getFechaFinalizacion();
        if (resumen.getUltimoRegistro() == null || (fecha != null && fecha.isAfter(resumen.getUltimoRegistro()))) {
            resumen.setUltimoRegistro(fecha);
        }

        if (entrada.getPesoMaximo().compareTo(resumen.getPesoMaximoHistorico()) > 0) {
            resumen.setPesoMaximoHistorico(entrada.getPesoMaximo());
        }

        resumen.setVolumenHistorico(scale(resumen.getVolumenHistorico().add(entrada.getVolumenTotal())));

        OtroEntrenoChartPointDto chartPoint = new OtroEntrenoChartPointDto();
        chartPoint.setFecha(entrada.getFecha());
        chartPoint.setValor(entrada.getPesoMaximo());
        resumen.getChartData().add(chartPoint);
    }

    private void ordenarColecciones(OtroEntrenoResumenDto resumen) {
        resumen.getEntradas().sort(Comparator.comparing(OtroEntrenoEntradaDto::getFecha, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(OtroEntrenoEntradaDto::getId, Comparator.nullsLast(String::compareTo)));
        resumen.setChartData(new ArrayList<>(resumen.getChartData().stream()
                .sorted(Comparator.comparing(OtroEntrenoChartPointDto::getFecha, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList()));
    }

    private String resolverClaveAgrupacion(RegistroEjercicioVO ejercicio) {
        Long catalogoId = obtenerCatalogoEjercicioId(ejercicio);
        if (catalogoId != null) {
            return "catalogo:" + catalogoId;
        }

        String idEjercicio = resolverIdEjercicio(ejercicio);
        if (idEjercicio != null) {
            return "ejercicio:" + idEjercicio;
        }

        return "nombre-grupo:" + normalizarClave(ejercicio.getNombre()) + "|" + normalizarClave(ejercicio.getGrupoMuscular());
    }

    private Long obtenerCatalogoEjercicioId(RegistroEjercicioVO ejercicio) {
        if (ejercicio.getEjercicioCatalogo() != null) {
            return ejercicio.getEjercicioCatalogo().getId();
        }

        PlantillaEjercicioVO plantillaEjercicio = ejercicio.getPlantillaEjercicio();
        if (plantillaEjercicio != null && plantillaEjercicio.getEjercicioCatalogo() != null) {
            return plantillaEjercicio.getEjercicioCatalogo().getId();
        }

        return null;
    }

    private String resolverIdEjercicio(RegistroEjercicioVO ejercicio) {
        if (ejercicio.getPlantillaEjercicio() != null) {
            if (ejercicio.getPlantillaEjercicio().getIdFrontend() != null
                    && !ejercicio.getPlantillaEjercicio().getIdFrontend().isBlank()) {
                return ejercicio.getPlantillaEjercicio().getIdFrontend().trim();
            }
            if (ejercicio.getPlantillaEjercicio().getId() != null) {
                return ejercicio.getPlantillaEjercicio().getId().toString();
            }
        }

        if (ejercicio.getIdFrontend() != null && !ejercicio.getIdFrontend().isBlank()) {
            return ejercicio.getIdFrontend().trim();
        }

        return null;
    }

    private String resolverIdEntrada(RegistroEjercicioVO ejercicio) {
        if (ejercicio.getIdFrontend() != null && !ejercicio.getIdFrontend().isBlank()) {
            return ejercicio.getIdFrontend().trim();
        }
        return ejercicio.getId() == null ? null : ejercicio.getId().toString();
    }

    private BigDecimal calcularVolumenSerie(RegistroSerieVO serie) {
        if (serie.getPeso() == null || serie.getRepeticiones() == null) {
            return ZERO;
        }

        return scale(serie.getPeso().multiply(BigDecimal.valueOf(serie.getRepeticiones())));
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String normalizarClave(String value) {
        if (value == null || value.isBlank()) {
            return "sin-valor";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-");
    }
}
