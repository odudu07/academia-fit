package com.exemplo.academia.dto;

import com.exemplo.academia.model.Reserva;
import com.exemplo.academia.model.StatusReserva;

import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long aulaId,
        String aulaNome,
        LocalDateTime dataHoraAula,
        StatusReserva status) {

    public static ReservaResponse from(Reserva r) {
        return new ReservaResponse(
                r.getId(),
                r.getAluno().getId(), r.getAluno().getNome(),
                r.getAula().getId(), r.getAula().getNome(), r.getAula().getDataHora(),
                r.getStatus());
    }
}
