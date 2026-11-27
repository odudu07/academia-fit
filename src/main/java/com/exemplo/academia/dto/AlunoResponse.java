package com.exemplo.academia.dto;

import com.exemplo.academia.model.Aluno;

import java.time.LocalDate;

public record AlunoResponse(Long id, String nome, String email, String cpf, LocalDate dataNascimento) {

    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getId(), a.getNome(), a.getEmail(), a.getCpf(), a.getDataNascimento());
    }
}
