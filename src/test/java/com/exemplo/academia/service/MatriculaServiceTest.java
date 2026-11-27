package com.exemplo.academia.service;

import com.exemplo.academia.dto.MatriculaRequest;
import com.exemplo.academia.dto.MatriculaResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.exception.RegraNegocioException;
import com.exemplo.academia.model.*;
import com.exemplo.academia.repository.AlunoRepository;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.PlanoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock MatriculaRepository matriculaRepository;
    @Mock AlunoRepository alunoRepository;
    @Mock PlanoRepository planoRepository;
    @InjectMocks MatriculaService service;

    private Aluno aluno;
    private Plano plano;

    @BeforeEach
    void setUp() {
        aluno = new Aluno("Carlos Lima", "carlos@email.com", "12345678901", LocalDate.of(1995, 5, 10));
        aluno.setId(1L);
        plano = new Plano("Trimestral", new BigDecimal("129.90"), 3);
        plano.setId(2L);
    }

    private void alunoEPlanoExistem() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(planoRepository.findById(2L)).thenReturn(Optional.of(plano));
    }

    private void matriculaAtivaExiste(boolean existe) {
        when(matriculaRepository.existsByAlunoIdAndStatusAndDataFimGreaterThanEqual(
                eq(1L), eq(StatusMatricula.ATIVA), any(LocalDate.class))).thenReturn(existe);
    }

    private Matricula matricula(StatusMatricula status) {
        Matricula m = new Matricula();
        m.setId(30L);
        m.setAluno(aluno);
        m.setPlano(plano);
        m.setDataInicio(LocalDate.now());
        m.setDataFim(LocalDate.now().plusMonths(3));
        m.setStatus(status);
        return m;
    }

    @Test
    @DisplayName("Deve matricular e calcular a data de término pela duração do plano")
    void deveMatricularComSucesso() {
        LocalDate inicio = LocalDate.now().plusDays(1);
        alunoEPlanoExistem();
        matriculaAtivaExiste(false);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> {
            Matricula m = inv.getArgument(0);
            m.setId(30L);
            return m;
        });

        MatriculaResponse resposta = service.matricular(new MatriculaRequest(1L, 2L, inicio));

        assertEquals(30L, resposta.id());
        assertEquals(inicio.plusMonths(3), resposta.dataFim());
        assertEquals(StatusMatricula.ATIVA, resposta.status());
    }

    @Test
    @DisplayName("Não deve matricular aluno menor de 16 anos")
    void naoDeveMatricularMenorDeIdade() {
        aluno.setDataNascimento(LocalDate.now().minusYears(10));
        alunoEPlanoExistem();

        assertThrows(RegraNegocioException.class,
                () -> service.matricular(new MatriculaRequest(1L, 2L, LocalDate.now())));

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve matricular aluno que já possui matrícula ativa")
    void naoDeveMatricularComMatriculaAtiva() {
        alunoEPlanoExistem();
        matriculaAtivaExiste(true);

        assertThrows(ConflitoException.class,
                () -> service.matricular(new MatriculaRequest(1L, 2L, LocalDate.now())));

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve matricular quando o aluno não existe")
    void naoDeveMatricularAlunoInexistente() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.matricular(new MatriculaRequest(1L, 2L, LocalDate.now())));
    }

    @Test
    @DisplayName("Deve cancelar matrícula ativa")
    void deveCancelarMatricula() {
        Matricula m = matricula(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(30L)).thenReturn(Optional.of(m));
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        MatriculaResponse resposta = service.cancelar(30L);

        assertEquals(StatusMatricula.CANCELADA, resposta.status());
    }

    @Test
    @DisplayName("Não deve cancelar matrícula já cancelada")
    void naoDeveCancelarMatriculaJaCancelada() {
        when(matriculaRepository.findById(30L)).thenReturn(Optional.of(matricula(StatusMatricula.CANCELADA)));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(30L));

        verify(matriculaRepository, never()).save(any());
    }
}
