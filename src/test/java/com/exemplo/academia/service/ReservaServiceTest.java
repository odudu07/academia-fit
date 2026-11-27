package com.exemplo.academia.service;

import com.exemplo.academia.dto.ReservaRequest;
import com.exemplo.academia.dto.ReservaResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.exception.RegraNegocioException;
import com.exemplo.academia.model.*;
import com.exemplo.academia.repository.AlunoRepository;
import com.exemplo.academia.repository.AulaRepository;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock ReservaRepository reservaRepository;
    @Mock AlunoRepository alunoRepository;
    @Mock AulaRepository aulaRepository;
    @Mock MatriculaRepository matriculaRepository;
    @InjectMocks ReservaService service;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        aluno = new Aluno("Carlos Lima", "carlos@email.com", "12345678901", LocalDate.of(1995, 5, 10));
        aluno.setId(1L);
    }

    private Aula aula(LocalDateTime dataHora, int capacidade) {
        Aula a = new Aula("Spinning", "Prof. Ana", dataHora, capacidade);
        a.setId(5L);
        return a;
    }

    private Reserva reserva(StatusReserva status, Aula aula) {
        Reserva r = new Reserva();
        r.setId(20L);
        r.setAluno(aluno);
        r.setAula(aula);
        r.setStatus(status);
        return r;
    }

    private void aluno_e_aula_existem(Aula aula) {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(aulaRepository.findById(5L)).thenReturn(Optional.of(aula));
    }

    private void matriculaVigente(Aula aula, boolean vigente) {
        when(matriculaRepository.existsByAlunoIdAndStatusAndDataFimGreaterThanEqual(
                eq(1L), eq(StatusMatricula.ATIVA), eq(aula.getDataHora().toLocalDate()))).thenReturn(vigente);
    }

    // ---------- Reservar ----------

    @Test
    @DisplayName("Deve reservar aula quando o aluno tem matrícula ativa e há vagas")
    void deveReservarComSucesso() {
        Aula aula = aula(LocalDateTime.now().plusDays(1), 10);
        aluno_e_aula_existem(aula);
        matriculaVigente(aula, true);
        when(reservaRepository.existsByAlunoIdAndAulaIdAndStatus(1L, 5L, StatusReserva.CONFIRMADA)).thenReturn(false);
        when(reservaRepository.countByAulaIdAndStatus(5L, StatusReserva.CONFIRMADA)).thenReturn(3L);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(20L);
            return r;
        });

        ReservaResponse resposta = service.reservar(new ReservaRequest(1L, 5L));

        assertEquals(20L, resposta.id());
        assertEquals(StatusReserva.CONFIRMADA, resposta.status());
        assertEquals("Spinning", resposta.aulaNome());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("Não deve reservar quando o aluno não existe")
    void naoDeveReservarAlunoInexistente() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.reservar(new ReservaRequest(1L, 5L)));

        verifyNoInteractions(reservaRepository);
    }

    @Test
    @DisplayName("Não deve reservar aula que já ocorreu")
    void naoDeveReservarAulaPassada() {
        Aula aula = aula(LocalDateTime.now().minusHours(3), 10);
        aluno_e_aula_existem(aula);

        assertThrows(RegraNegocioException.class, () -> service.reservar(new ReservaRequest(1L, 5L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve reservar quando o aluno não tem matrícula ativa")
    void naoDeveReservarSemMatriculaAtiva() {
        Aula aula = aula(LocalDateTime.now().plusDays(1), 10);
        aluno_e_aula_existem(aula);
        matriculaVigente(aula, false);

        assertThrows(RegraNegocioException.class, () -> service.reservar(new ReservaRequest(1L, 5L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve reservar a mesma aula duas vezes")
    void naoDeveReservarDuplicada() {
        Aula aula = aula(LocalDateTime.now().plusDays(1), 10);
        aluno_e_aula_existem(aula);
        matriculaVigente(aula, true);
        when(reservaRepository.existsByAlunoIdAndAulaIdAndStatus(1L, 5L, StatusReserva.CONFIRMADA)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.reservar(new ReservaRequest(1L, 5L)));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve reservar aula lotada")
    void naoDeveReservarAulaLotada() {
        Aula aula = aula(LocalDateTime.now().plusDays(1), 10);
        aluno_e_aula_existem(aula);
        matriculaVigente(aula, true);
        when(reservaRepository.existsByAlunoIdAndAulaIdAndStatus(1L, 5L, StatusReserva.CONFIRMADA)).thenReturn(false);
        when(reservaRepository.countByAulaIdAndStatus(5L, StatusReserva.CONFIRMADA)).thenReturn(10L);

        assertThrows(ConflitoException.class, () -> service.reservar(new ReservaRequest(1L, 5L)));

        verify(reservaRepository, never()).save(any());
    }

    // ---------- Cancelar ----------

    @Test
    @DisplayName("Deve cancelar reserva com mais de 2h de antecedência")
    void deveCancelarComAntecedencia() {
        Reserva r = reserva(StatusReserva.CONFIRMADA, aula(LocalDateTime.now().plusHours(5), 10));
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(r));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse resposta = service.cancelar(20L);

        assertEquals(StatusReserva.CANCELADA, resposta.status());
    }

    @Test
    @DisplayName("Não deve cancelar reserva com menos de 2h de antecedência")
    void naoDeveCancelarComMenosDe2h() {
        Reserva r = reserva(StatusReserva.CONFIRMADA, aula(LocalDateTime.now().plusHours(1), 10));
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(r));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(20L));

        assertEquals(StatusReserva.CONFIRMADA, r.getStatus());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve cancelar reserva já cancelada")
    void naoDeveCancelarReservaJaCancelada() {
        Reserva r = reserva(StatusReserva.CANCELADA, aula(LocalDateTime.now().plusDays(2), 10));
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(r));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(20L));
    }

    @Test
    @DisplayName("Deve lançar 404 ao cancelar reserva inexistente")
    void naoDeveCancelarReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.cancelar(99L));
    }
}
