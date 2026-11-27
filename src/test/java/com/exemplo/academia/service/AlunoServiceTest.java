package com.exemplo.academia.service;

import com.exemplo.academia.dto.AlunoRequest;
import com.exemplo.academia.dto.AlunoResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.model.Aluno;
import com.exemplo.academia.repository.AlunoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock AlunoRepository repository;
    @InjectMocks AlunoService service;

    private final AlunoRequest request =
            new AlunoRequest("Carlos Lima", "carlos@email.com", "12345678901", LocalDate.of(1995, 5, 10));

    @Test
    void deveCriarAluno() {
        when(repository.existsByEmail("carlos@email.com")).thenReturn(false);
        when(repository.existsByCpf("12345678901")).thenReturn(false);
        when(repository.save(any(Aluno.class))).thenAnswer(inv -> {
            Aluno a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AlunoResponse resposta = service.criar(request);

        assertEquals(1L, resposta.id());
        assertEquals("Carlos Lima", resposta.nome());
    }

    @Test
    void naoDeveCriarAlunoComEmailDuplicado() {
        when(repository.existsByEmail("carlos@email.com")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.criar(request));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarAlunoComCpfDuplicado() {
        when(repository.existsByEmail("carlos@email.com")).thenReturn(false);
        when(repository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.criar(request));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarAlunoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(99L, request));
    }

    @Test
    void deveAtualizarAluno() {
        Aluno existente = new Aluno("Antigo", "antigo@email.com", "00000000000", LocalDate.of(1990, 1, 1));
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("carlos@email.com", 1L)).thenReturn(false);
        when(repository.existsByCpfAndIdNot("12345678901", 1L)).thenReturn(false);
        when(repository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        AlunoResponse resposta = service.atualizar(1L, request);

        assertEquals("Carlos Lima", resposta.nome());
        assertEquals("carlos@email.com", resposta.email());
    }
}
