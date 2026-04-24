package com.pesapp.pesapp.entrenamientos.service;

import com.pesapp.pesapp.entrenamientos.model.dto.OtroEntrenoResumenDto;
import java.util.List;

public interface OtrosEntrenosService {

    List<OtroEntrenoResumenDto> obtenerHistoricoAgrupado();
}
