package com.exemplo.academia.dto;

import com.exemplo.academia.model.Matricula;
import com.exemplo.academia.model.StatusMatricula;

import java.time.LocalDate;

public record MatriculaResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long planoId,
        String planoNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusMatricula status) {

    public static MatriculaResponse from(Matricula m) {
        return new MatriculaResponse(
                m.getId(),
                m.getAluno().getId(), m.getAluno().getNome(),
                m.getPlano().getId(), m.getPlano().getNome(),
                m.getDataInicio(), m.getDataFim(), m.getStatus());
    }
}
