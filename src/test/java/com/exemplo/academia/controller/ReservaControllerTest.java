package com.exemplo.academia.controller;

import com.exemplo.academia.dto.ReservaResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RegraNegocioException;
import com.exemplo.academia.model.StatusReserva;
import com.exemplo.academia.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ReservaService service;

    private static final String JSON_VALIDO = """
            {"alunoId": 1, "aulaId": 5}
            """;

    @Test
    void deveRetornar201AoReservar() throws Exception {
        when(service.reservar(any())).thenReturn(new ReservaResponse(
                20L, 1L, "Carlos", 5L, "Spinning", LocalDateTime.now().plusDays(1), StatusReserva.CONFIRMADA));

        mockMvc.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void deveRetornar400QuandoCamposAusentes() throws Exception {
        mockMvc.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.alunoId").exists())
                .andExpect(jsonPath("$.detalhes.aulaId").exists());

        verifyNoInteractions(service);
    }

    @Test
    void deveRetornar409QuandoAulaLotada() throws Exception {
        when(service.reservar(any())).thenThrow(new ConflitoException("A aula está lotada"));

        mockMvc.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("A aula está lotada"));
    }

    @Test
    void deveRetornar422QuandoSemMatriculaAtiva() throws Exception {
        when(service.reservar(any())).thenThrow(new RegraNegocioException("Sem matrícula ativa"));

        mockMvc.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar200AoCancelar() throws Exception {
        when(service.cancelar(20L)).thenReturn(new ReservaResponse(
                20L, 1L, "Carlos", 5L, "Spinning", LocalDateTime.now().plusDays(1), StatusReserva.CANCELADA));

        mockMvc.perform(patch("/reservas/20/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }
}
