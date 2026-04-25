package com.pesapp.pesapp.peso.service;

import com.pesapp.pesapp.peso.model.dto.PesoCorporalRequestDto;
import com.pesapp.pesapp.peso.model.dto.PesoCorporalResponseDto;
import com.pesapp.pesapp.peso.model.dto.PesoHoyRequestDto;
import java.util.List;

public interface PesoCorporalService {

    List<PesoCorporalResponseDto> obtenerHistorico();

    PesoCorporalResponseDto guardarPesoHoy(PesoHoyRequestDto request);

    PesoCorporalResponseDto guardar(PesoCorporalRequestDto request);
}
