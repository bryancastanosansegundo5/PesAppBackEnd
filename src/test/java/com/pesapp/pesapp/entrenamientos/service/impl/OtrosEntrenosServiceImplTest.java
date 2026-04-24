package com.pesapp.pesapp.entrenamientos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoResumenDto;
import com.pesapp.pesapp.entrenamientos.model.vo.EjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.PlantillaEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEjercicioVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroEntrenamientoVO;
import com.pesapp.pesapp.entrenamientos.model.vo.RegistroSerieVO;
import com.pesapp.pesapp.entrenamientos.repository.RegistroEjercicioRepository;
import com.pesapp.pesapp.usuarios.model.vo.UsuarioVO;
import com.pesapp.pesapp.usuarios.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtrosEntrenosServiceImplTest {

    @Mock
    private RegistroEjercicioRepository registroEjercicioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OtrosEntrenosServiceImpl otrosEntrenosService;

    @Test
    void debeAgruparCalcularMetricasYOrdenarEntradas() {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(7L);

        RegistroEjercicioVO reciente = crearEjercicioCatalogado(
                101L,
                "press-banca-1",
                44L,
                "Press banca",
                "Pecho",
                LocalDateTime.of(2026, 4, 20, 19, 0),
                "Empuje horizontal");
        reciente.setDescripcion("Barra olimpica");
        reciente.setEquipamiento("Barra");
        reciente.setAgarre("Pronado");
        reciente.setAlturaBanco("Plano");
        reciente.setSeriesBase(3);
        reciente.setRepeticionesBase(8);
        reciente.setPesoBase(new BigDecimal("82.50"));
        reciente.setSeriesRealizadas(List.of(
                crearSerie(1, 8, "80"),
                crearSerie(2, 6, "85")));

        RegistroEjercicioVO antiguo = crearEjercicioCatalogado(
                102L,
                "press-banca-2",
                44L,
                "Press banca",
                "Pecho",
                LocalDateTime.of(2026, 4, 10, 19, 0),
                "Empuje horizontal");
        antiguo.setSeriesBase(4);
        antiguo.setRepeticionesBase(6);
        antiguo.setPesoBase(new BigDecimal("78.00"));
        antiguo.setAgarre("Pronado");
        antiguo.setAlturaBanco("Plano");
        antiguo.setSeriesRealizadas(List.of(
                crearSerie(1, 6, "75"),
                crearSerie(2, 6, "77.5")));

        RegistroEjercicioVO sinCatalogo = crearEjercicioSinCatalogo(
                201L,
                null,
                501L,
                null,
                "Curl mancuerna",
                "Biceps",
                LocalDateTime.of(2026, 4, 18, 18, 30));
        sinCatalogo.setSeriesBase(3);
        sinCatalogo.setRepeticionesBase(12);
        sinCatalogo.setPesoBase(new BigDecimal("14"));
        sinCatalogo.setSeriesRealizadas(List.of(
                crearSerie(1, 12, "14"),
                crearSerie(2, 10, "16")));

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(registroEjercicioRepository.findHistoricoVisibleByUsuarioId(7L))
                .thenReturn(List.of(reciente, antiguo, sinCatalogo));

        List<OtroEntrenoResumenDto> respuesta = otrosEntrenosService.obtenerHistoricoAgrupado();

        assertThat(respuesta).hasSize(2);

        OtroEntrenoResumenDto pressBanca = respuesta.getFirst();
        assertThat(pressBanca.getId()).isEqualTo("catalogo:44");
        assertThat(pressBanca.getNombre()).isEqualTo("Press banca");
        assertThat(pressBanca.getSesionesTotales()).isEqualTo(2);
        assertThat(pressBanca.getUltimoRegistro()).isEqualTo(LocalDateTime.of(2026, 4, 20, 19, 0));
        assertThat(pressBanca.getPesoMaximoHistorico()).isEqualByComparingTo("85.00");
        assertThat(pressBanca.getVolumenHistorico()).isEqualByComparingTo("2065.00");
        assertThat(pressBanca.getEntradas()).hasSize(2);
        assertThat(pressBanca.getEntradas().get(0).getId()).isEqualTo("press-banca-1");
        assertThat(pressBanca.getEntradas().get(0).getRepeticionesTotales()).isEqualTo(14);
        assertThat(pressBanca.getEntradas().get(0).getVolumenTotal()).isEqualByComparingTo("1150.00");
        assertThat(pressBanca.getEntradas().get(0).getPesoMaximo()).isEqualByComparingTo("85.00");
        assertThat(pressBanca.getEntradas().get(1).getVolumenTotal()).isEqualByComparingTo("915.00");
        assertThat(pressBanca.getChartData()).hasSize(2);
        assertThat(pressBanca.getChartData().get(0).getFecha()).isEqualTo(LocalDateTime.of(2026, 4, 10, 19, 0));
        assertThat(pressBanca.getChartData().get(1).getValor()).isEqualByComparingTo("85.00");

        OtroEntrenoResumenDto curl = respuesta.get(1);
        assertThat(curl.getId()).isEqualTo("ejercicio:501");
        assertThat(curl.getSesionesTotales()).isEqualTo(1);
        assertThat(curl.getPesoMaximoHistorico()).isEqualByComparingTo("16.00");
        assertThat(curl.getVolumenHistorico()).isEqualByComparingTo("328.00");
    }

    @Test
    void debeUsarFallbackNombreGrupoCuandoNoHayCatalogoNiIdEjercicio() {
        UsuarioVO usuario = new UsuarioVO();
        usuario.setId(8L);

        RegistroEjercicioVO ejercicio = crearEjercicioSinCatalogo(
                301L,
                null,
                null,
                null,
                "Remo en polea",
                "Espalda Alta",
                LocalDateTime.of(2026, 4, 5, 12, 0));
        ejercicio.setSeriesRealizadas(List.of(crearSerie(1, 10, "45")));

        when(usuarioService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(registroEjercicioRepository.findHistoricoVisibleByUsuarioId(8L)).thenReturn(List.of(ejercicio));

        List<OtroEntrenoResumenDto> respuesta = otrosEntrenosService.obtenerHistoricoAgrupado();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.getFirst().getId()).isEqualTo("nombre-grupo:remo-en-polea|espalda-alta");
    }

    private RegistroEjercicioVO crearEjercicioCatalogado(
            Long id,
            String idFrontend,
            Long catalogoId,
            String nombre,
            String grupoMuscular,
            LocalDateTime fecha,
            String patronMovimiento) {
        RegistroEjercicioVO ejercicio = new RegistroEjercicioVO();
        ejercicio.setId(id);
        ejercicio.setIdFrontend(idFrontend);
        ejercicio.setNombre(nombre);
        ejercicio.setGrupoMuscular(grupoMuscular);
        ejercicio.setPatronMovimiento(patronMovimiento);
        ejercicio.setRegistroEntrenamiento(crearEntrenamiento(fecha));
        ejercicio.setEjercicioCatalogo(crearCatalogo(catalogoId));
        return ejercicio;
    }

    private RegistroEjercicioVO crearEjercicioSinCatalogo(
            Long id,
            String idFrontend,
            Long plantillaId,
            String plantillaFrontendId,
            String nombre,
            String grupoMuscular,
            LocalDateTime fecha) {
        RegistroEjercicioVO ejercicio = new RegistroEjercicioVO();
        ejercicio.setId(id);
        ejercicio.setIdFrontend(idFrontend);
        ejercicio.setNombre(nombre);
        ejercicio.setGrupoMuscular(grupoMuscular);
        ejercicio.setRegistroEntrenamiento(crearEntrenamiento(fecha));
        if (plantillaId != null || plantillaFrontendId != null) {
            PlantillaEjercicioVO plantilla = new PlantillaEjercicioVO();
            plantilla.setId(plantillaId);
            plantilla.setIdFrontend(plantillaFrontendId);
            ejercicio.setPlantillaEjercicio(plantilla);
        }
        return ejercicio;
    }

    private RegistroEntrenamientoVO crearEntrenamiento(LocalDateTime fecha) {
        RegistroEntrenamientoVO entrenamiento = new RegistroEntrenamientoVO();
        entrenamiento.setNombreSesion("Sesion A");
        entrenamiento.setFechaFinalizacion(fecha);
        return entrenamiento;
    }

    private EjercicioVO crearCatalogo(Long id) {
        EjercicioVO ejercicio = new EjercicioVO();
        ejercicio.setId(id);
        return ejercicio;
    }

    private RegistroSerieVO crearSerie(int orden, int repeticiones, String peso) {
        RegistroSerieVO serie = new RegistroSerieVO();
        serie.setOrden(orden);
        serie.setNumeroSerie(orden);
        serie.setRepeticiones(repeticiones);
        serie.setPeso(new BigDecimal(peso));
        return serie;
    }
}
