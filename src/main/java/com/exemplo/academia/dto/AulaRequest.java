package com.exemplo.academia.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record AulaRequest(
        @NotBlank(message = "O nome da aula é obrigatório")
        String nome,

        @NotBlank(message = "O instrutor é obrigatório")
        String instrutor,

        @NotNull(message = "A data e hora são obrigatórias")
        @Future(message = "A data e hora devem estar no futuro")
        LocalDateTime dataHora,

        @NotNull(message = "A capacidade é obrigatória")
        @Min(value = 1, message = "A capacidade mínima é 1")
        @Max(value = 100, message = "A capacidade máxima é 100")
        Integer capacidade) {
}
