package com.exemplo.academia.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MatriculaRequest(
        @NotNull(message = "O aluno é obrigatório")
        Long alunoId,

        @NotNull(message = "O plano é obrigatório")
        Long planoId,

        @NotNull(message = "A data de início é obrigatória")
        @FutureOrPresent(message = "A data de início não pode estar no passado")
        LocalDate dataInicio) {
}
