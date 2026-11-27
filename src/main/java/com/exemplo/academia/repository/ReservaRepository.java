package com.exemplo.academia.repository;

import com.exemplo.academia.model.Reserva;
import com.exemplo.academia.model.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    long countByAulaIdAndStatus(Long aulaId, StatusReserva status);

    boolean existsByAlunoIdAndAulaIdAndStatus(Long alunoId, Long aulaId, StatusReserva status);

    List<Reserva> findByAlunoId(Long alunoId);
}
