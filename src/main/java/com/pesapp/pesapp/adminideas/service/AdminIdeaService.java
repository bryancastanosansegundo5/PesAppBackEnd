package com.pesapp.pesapp.adminideas.service;

import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaCreateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaEstadoUpdateRequestDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaResponseDto;
import com.pesapp.pesapp.adminideas.model.dto.AdminIdeaUpdateRequestDto;
import java.util.List;

public interface AdminIdeaService {

    List<AdminIdeaResponseDto> obtenerIdeas();

    AdminIdeaResponseDto guardarIdea(AdminIdeaCreateRequestDto request);

    AdminIdeaResponseDto actualizarIdea(Long id, AdminIdeaUpdateRequestDto request);

    AdminIdeaResponseDto actualizarEstado(Long id, AdminIdeaEstadoUpdateRequestDto request);
}
