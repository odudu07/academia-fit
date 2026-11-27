package com.exemplo.academia.service;

import com.exemplo.academia.dto.ReservaRequest;
import com.exemplo.academia.dto.ReservaResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.exception.RegraNegocioException;
import com.exemplo.academia.model.Aluno;
import com.exemplo.academia.model.Aula;
import com.exemplo.academia.model.Reserva;
import com.exemplo.academia.model.StatusMatricula;
import com.exemplo.academia.model.StatusReserva;
import com.exemplo.academia.repository.AlunoRepository;
import com.exemplo.academia.repository.AulaRepository;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negócio:
 * 1. Não é possível reservar aula que já ocorreu.
 * 2. O aluno precisa ter matrícula ativa e vigente na data da aula.
 * 3. O aluno não pode reservar a mesma aula duas vezes.
 * 4. A aula não pode exceder a capacidade (lotação).
 * 5. O cancelamento da reserva exige no mínimo 2h de antecedência.
 */
@Service
@Transactional
public class ReservaService {

    static final long HORAS_MINIMAS_CANCELAMENTO = 2;

    private final ReservaRepository reservaRepository;
    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;
    private final MatriculaRepository matriculaRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          AlunoRepository alunoRepository,
                          AulaRepository aulaRepository,
                          MatriculaRepository matriculaRepository) {
        this.reservaRepository = reservaRepository;
        this.alunoRepository = alunoRepository;
        this.aulaRepository = aulaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public ReservaResponse reservar(ReservaRequest request) {
        Aluno aluno = alunoRepository.findById(request.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + request.alunoId()));
        Aula aula = aulaRepository.findById(request.aulaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada: " + request.aulaId()));

        if (!aula.getDataHora().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível reservar uma aula que já ocorreu");
        }

        if (!matriculaRepository.existsByAlunoIdAndStatusAndDataFimGreaterThanEqual(
                aluno.getId(), StatusMatricula.ATIVA, aula.getDataHora().toLocalDate())) {
            throw new RegraNegocioException("O aluno não possui matrícula ativa para a data da aula");
        }

        if (reservaRepository.existsByAlunoIdAndAulaIdAndStatus(
                aluno.getId(), aula.getId(), StatusReserva.CONFIRMADA)) {
            throw new ConflitoException("O aluno já possui reserva confirmada nesta aula");
        }

        long ocupadas = reservaRepository.countByAulaIdAndStatus(aula.getId(), StatusReserva.CONFIRMADA);
        if (ocupadas >= aula.getCapacidade()) {
            throw new ConflitoException("A aula está lotada");
        }

        Reserva reserva = new Reserva();
        reserva.setAluno(aluno);
        reserva.setAula(aula);
        reserva.setStatus(StatusReserva.CONFIRMADA);

        return ReservaResponse.from(reservaRepository.save(reserva));
    }

    public ReservaResponse cancelar(Long id) {
        Reserva reserva = buscarEntidade(id);

        if (reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw new RegraNegocioException("A reserva já está cancelada");
        }
        if (reserva.getAula().getDataHora().isBefore(LocalDateTime.now().plusHours(HORAS_MINIMAS_CANCELAMENTO))) {
            throw new RegraNegocioException(
                    "O cancelamento exige no mínimo " + HORAS_MINIMAS_CANCELAMENTO + "h de antecedência");
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        return ReservaResponse.from(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public ReservaResponse buscar(Long id) {
        return ReservaResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(Long alunoId) {
        List<Reserva> reservas = alunoId != null
                ? reservaRepository.findByAlunoId(alunoId)
                : reservaRepository.findAll();
        return reservas.stream().map(ReservaResponse::from).toList();
    }

    private Reserva buscarEntidade(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada: " + id));
    }
}
