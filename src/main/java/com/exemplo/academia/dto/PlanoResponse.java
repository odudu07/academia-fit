package com.exemplo.academia.dto;

import com.exemplo.academia.model.Plano;

import java.math.BigDecimal;

public record PlanoResponse(Long id, String nome, BigDecimal valorMensal, Integer duracaoMeses) {

    public static PlanoResponse from(Plano p) {
        return new PlanoResponse(p.getId(), p.getNome(), p.getValorMensal(), p.getDuracaoMeses());
    }
}
