package com.exemplo.academia.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        Map<String, String> detalhes) {

    public static ErroResponse of(int status, String mensagem, Map<String, String> detalhes) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, detalhes);
    }
}
