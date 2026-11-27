package com.exemplo.academia.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PlanoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O valor mensal é obrigatório")
        @Positive(message = "O valor mensal deve ser maior que zero")
        BigDecimal valorMensal,

        @NotNull(message = "A duração é obrigatória")
        @Min(value = 1, message = "A duração mínima é de 1 mês")
        @Max(value = 24, message = "A duração máxima é de 24 meses")
        Integer duracaoMeses) {
}
