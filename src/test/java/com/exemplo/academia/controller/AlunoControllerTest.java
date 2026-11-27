package com.exemplo.academia.controller;

import com.exemplo.academia.dto.AlunoResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.service.AlunoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlunoController.class)
class AlunoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AlunoService service;

    private static final String JSON_VALIDO = """
            {"nome": "Carlos Lima", "email": "carlos@email.com", "cpf": "12345678901", "dataNascimento": "1995-05-10"}
            """;

    @Test
    void deveRetornar201AoCadastrar() throws Exception {
        when(service.criar(any())).thenReturn(
                new AlunoResponse(1L, "Carlos Lima", "carlos@email.com", "12345678901", LocalDate.of(1995, 5, 10)));

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        String invalido = """
                {"nome": "", "email": "nao-e-email", "cpf": "123", "dataNascimento": "2999-01-01"}
                """;

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.nome").exists())
                .andExpect(jsonPath("$.detalhes.email").exists())
                .andExpect(jsonPath("$.detalhes.cpf").exists())
                .andExpect(jsonPath("$.detalhes.dataNascimento").exists());

        verifyNoInteractions(service);
    }

    @Test
    void deveRetornar409QuandoEmailDuplicado() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("E-mail já cadastrado"));

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));
    }

    @Test
    void deveRetornar404QuandoAlunoNaoExiste() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Aluno não encontrado: 99"));

        mockMvc.perform(get("/alunos/99")).andExpect(status().isNotFound());
    }
}
