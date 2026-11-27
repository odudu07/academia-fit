package com.exemplo.academia.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull(message = "O aluno é obrigatório")
        Long alunoId,

        @NotNull(message = "A aula é obrigatória")
        Long aulaId) {
}
