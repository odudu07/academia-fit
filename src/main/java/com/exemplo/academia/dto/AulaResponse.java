package com.exemplo.academia.dto;

import com.exemplo.academia.model.Aula;

import java.time.LocalDateTime;

public record AulaResponse(
        Long id,
        String nome,
        String instrutor,
        LocalDateTime dataHora,
        Integer capacidade,
        long vagasDisponiveis) {

    public static AulaResponse from(Aula a, long reservasConfirmadas) {
        return new AulaResponse(a.getId(), a.getNome(), a.getInstrutor(), a.getDataHora(),
                a.getCapacidade(), a.getCapacidade() - reservasConfirmadas);
    }
}
